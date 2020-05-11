//

import Foundation
import UIKit
import Foundation

class AppOpener {

    
    func checkOpenAppEvent(idIos:String, nombreIos: String, command: String) -> Any {
        let url = nombreIos + idIos
        let appURL = URL(string: url)
        print(UIApplication.shared.open(appURL! as URL))
             

    
            if UIApplication.shared.canOpenURL(appURL! as URL) {
                   if #available(iOS 10.0, *) {
                       let isOpen = openApp(idIos: idIos,nombreIos: nombreIos, command: command)
                       return SendPlugin().sendPluginSuccess(response: "\(isOpen)", command: command)
                  
                   } else {
                      return SendPlugin().sendPluginError(code: 105, description: "El S.O. tiene que ser mínimo v10.0.", command: command)
                  
                   }
               } else {
                
                 let isOpen = openMarket(idIos: idIos, nombreIos: nombreIos, command: command)
                 return SendPlugin().sendPluginSuccess(response: "\(isOpen)", command: command)
             
               }
     
    }
    func checkOpenAppProfileEvent(idIos:String, nombreIos: String, userProfile:String, command: String) -> Any {
       
             var url: String = ""
             switch nombreIos {
                 case "fb://":
                     url = "fb://profile/" + userProfile
                 case "twitter://":
                     url = "twitter://user?screen_name=" + userProfile
                 default:
              
                     return SendPlugin().sendPluginError(code: 104, description: "No se puede abrir otra aplicación. (iOS)", command: command)
             }
             
             let appURL = URL(string: url)!
             print(UIApplication.shared.open(appURL as URL, options: [:], completionHandler: nil))
    
             if UIApplication.shared.canOpenURL(appURL as URL) {
                 if #available(iOS 10.0, *) {

                     UIApplication.shared.open(appURL as URL, options: [:], completionHandler: nil)
                     let isOpen = openProfileApp(idIos: idIos, nombreIos: nombreIos, userProfile: userProfile, command: command)
                     return SendPlugin().sendPluginSuccess(response: "\(isOpen)", command: command)
                 } else {
                     return SendPlugin().sendPluginError(code: 105, description: "El S.O. tiene que ser mínimo v10.0.", command: command)
                   }
             } else {
                    let isOpen = openMarket(idIos: idIos, nombreIos: nombreIos, command: command)
                    return SendPlugin().sendPluginSuccess(response: "\(isOpen)", command: command)
             }
            
    }
    
    func openApp(idIos:String, nombreIos: String, command: String)->Any  {
            let url = nombreIos + idIos
            let appURL = URL(string: url)
            UIApplication.shared.open(appURL! as URL)
            return SendPlugin().sendPluginSuccess(response: "", command: command)
    }
    
    func openProfileApp(idIos:String, nombreIos: String, userProfile:String, command: String)-> Any{
            let nombreIos = "fb://"
            var url: String = ""
            switch nombreIos {
                case "fb://":
                    url = "fb://profile/" + userProfile
                case "twitter://":
                    url = "twitter://user?screen_name=" + userProfile
                default:
             
                    return SendPlugin().sendPluginError(code: 104, description: "No se puede abrir otra aplicación. (iOS)", command: command)
            }
            
            let appURL = URL(string: url)!
            UIApplication.shared.open(appURL as URL, options: [:], completionHandler: nil)
            return SendPlugin().sendPluginSuccess(response: "", command: command)
    }
    
    func openMarket(idIos:String, nombreIos: String, command: String) -> Any{
        let url = URL(string: "itms-apps://itunes.apple.com/app/id" + idIos)
        
        if #available(iOS 10.0, *) {
            
            UIApplication.shared.open(url!)
    
            return ""
        } else {
            
            return SendPlugin().sendPluginError(code: 105, description: "El S.O. tiene que ser mínimo v10.0.", command: command)
        }
    }
    
    func shareToApp(text:String, command: String) -> Any {
        let esURL = verifyUrl(urlString: text)
        if(esURL){
           let url = URL(string: text)!
            print("Download Started")
            getData(from: url) { data, response, error in
                guard let data = data, error == nil else { return }
                let path: String = url.pathExtension
                print(path)
                print(response?.suggestedFilename ?? url.lastPathComponent)
                print("Download Finished")
                DispatchQueue.main.async() {
                    if (path == "jpg" || path == "png" ){
                        UIImage(data: data).share()
                    }else{
                        data.share()
                    }
                    
                }
            }
            return SendPlugin().sendPluginSuccess(response: "OK", command: command)
        }else{
            text.share()
            return SendPlugin().sendPluginSuccess(response: "OK", command: command)
        }
      }
    
    func getData(from url: URL, completion: @escaping (Data?, URLResponse?, Error?) -> ()) {
        URLSession.shared.dataTask(with: url, completionHandler: completion).resume()
    }
    
   
    
    // Swift 5
     func verifyUrl (urlString: String?) -> Bool {
        if let urlString = urlString {
            if let url = NSURL(string: urlString) {
                return UIApplication.shared.canOpenURL(url as URL)
            }
        }
        return false
    }
    
    
    
}

extension UIApplication {
    class var topViewController: UIViewController? { return getTopViewController() }
    private class func getTopViewController(base: UIViewController? = UIApplication.shared.keyWindow?.rootViewController) -> UIViewController? {
        if let nav = base as? UINavigationController { return getTopViewController(base: nav.visibleViewController) }
        if let tab = base as? UITabBarController {
            if let selected = tab.selectedViewController { return getTopViewController(base: selected) }
        }
        if let presented = base?.presentedViewController { return getTopViewController(base: presented) }
        return base
    }
}

extension Hashable {
    func share() {
        let activity = UIActivityViewController(activityItems: [self], applicationActivities: nil)
        UIApplication.topViewController?.present(activity, animated: true, completion: nil)
    }
}

