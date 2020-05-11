//
//  ViewController.swift
//  NuevaBanca-ios
//
//  Created by Daniel Parra Crespo on 20/02/2020.
//  Copyright © 2020 Daniel Parra Crespo. All rights reserved.
//

import UIKit
import WebKit
import Firebase
import Darwin
import UserNotifications
import LocalAuthentication
import Contacts
import ContactsUI


class ViewController: UIViewController, WKScriptMessageHandler, UIWebViewDelegate, CNContactPickerDelegate {
    
    @IBOutlet weak var keyboardHeightLayoutConstraint: NSLayoutConstraint!
    @IBOutlet weak var webView: WKWebView!
    
    var adapterName: String = "nbAdapter"
    var utils: Utils = Utils()
    var appopener: AppOpener  = AppOpener()
    var biometrics: Biometric = Biometric()
    var push: Push = Push()

    let gcmMessageIDKey = "gcm.message_id"
    
    override func viewDidDisappear(_ animated: Bool) {
        let data = DataResponse()
        data.response = "background"
        let extra = Extra(codigoRetorno: 1, data: data)
        let event = Event(event: "nbe-listenForeground", extra: extra)
        sendMessageToWebView(evento:event)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        let data = DataResponse()
        data.response = "background"
        let extra = Extra(codigoRetorno: 1, data: data)
        let event = Event(event: "nbe-listenForeground", extra: extra)
        sendMessageToWebView(evento:event)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        let data = DataResponse()
        data.response = "foreground"
        let extra = Extra(codigoRetorno: 1, data: data)
        let event = Event(event: "nbe-listenForeground", extra: extra)
        sendMessageToWebView(evento:event)
    }
    override func viewWillAppear(_ animated: Bool) {
        let data = DataResponse()
        data.response = "foreground"
        let extra = Extra(codigoRetorno: 1, data: data)
        let event = Event(event: "nbe-listenForeground", extra: extra)
        sendMessageToWebView(evento:event)
       
    }
    
  
    
    // Añade la ruta para abrir en el WebView e inicializa
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //        DEVELOP
        //        let urlWeb = "https://cdntest.ruralvia.com/CA-FRONT/NBE/app/"
        //        TEST
        //                let urlWeb = "https://cdntest.ruralvia.com/CA-FRONT/NBE/app/develop/"
        //        PROD
                        let urlWeb = "https://cdn.rm-static.com/CA-FRONT/NBE/app/"
        //        PROPIA WEB
        //        let urlWeb = "https://pruebawebview.000webhostapp.com/"
        //        let urlWeb = "https://cdntest.ruralvia.com/CA-FRONT/NBE/app/feature-CAFE-0-prueba-webview-nativo/#/splash"

        //        let urlWeb = "https://pruebawebview.000webhostapp.com/"
        //        let urlWeb = "https://cdntest.ruralvia.com/CA-FRONT/NBE/app/feature-CAFE-0-prueba-webview-nativo/#/splash"
        //           let urlWeb = "https://nbeevents.000webhostapp.com/"
        
     
        
        
        let data = DataResponse()
        data.response = "foreground"
        let extra = Extra(codigoRetorno: 1, data: data)
        let event = Event(event: "nbe-listenForeground", extra: extra)
        sendMessageToWebView(evento:event)
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(self.keyboardNotification(notification:)),
                                               name: UIResponder.keyboardWillChangeFrameNotification,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(statusManager),
                                               name: .flagsChanged,
                                               object: nil)
 
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(didReceiveData),
                                               name: .didReceiveData,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(checkIfIsAccessedFromChecklist),
                                               name: .isAccessedFromChecklistVC,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(lowBattery(notification:)),
                                               name: UIDevice.batteryLevelDidChangeNotification,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(proximityStateDidChange(notification:)),
                                               name: UIDevice.proximityStateDidChangeNotification,
                                               object: nil)
        
  
        let url = URL(string: urlWeb)!
        webView.load(URLRequest(url: url))
        webView.allowsBackForwardNavigationGestures = true
        webView.scrollView.isScrollEnabled = false
        webView.scrollView.alwaysBounceHorizontal = false
        webView.scrollView.alwaysBounceVertical = false
        webView.translatesAutoresizingMaskIntoConstraints = false
        webView.scrollView.bounces = false
        webView.isOpaque = false
        webView.configuration.userContentController.add(self, name: adapterName)
    }
    
    @objc func keyboardNotification(notification: NSNotification) {
        if let userInfo = notification.userInfo {
            let endFrame = (userInfo[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue
            let endFrameY = endFrame?.origin.y ?? 0
            let duration:TimeInterval = (userInfo[UIResponder.keyboardAnimationDurationUserInfoKey] as? NSNumber)?.doubleValue ?? 0
            let animationCurveRawNSN = userInfo[UIResponder.keyboardAnimationCurveUserInfoKey] as? NSNumber
            let animationCurveRaw = animationCurveRawNSN?.uintValue ?? UIView.AnimationOptions.curveEaseInOut.rawValue
            let animationCurve:UIView.AnimationOptions = UIView.AnimationOptions(rawValue: animationCurveRaw)
            if endFrameY >= UIScreen.main.bounds.size.height {
                
                
                self.keyboardHeightLayoutConstraint?.constant = 0.0
                
            } else {
                self.keyboardHeightLayoutConstraint?.constant = endFrame?.size.height ?? 0.0
                
                
            }
            UIView.animate(withDuration: duration,
                           delay: TimeInterval(0),
                           options: animationCurve,
                           animations: { self.view.layoutIfNeeded() },
                           completion: nil)
        }
    }
    
    // Listener donde llegan los eventos lanzados por el WebView
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        print("En el UserController")
        print(message)
        print(message.name)
        print(message.body)
        var data: [String : AnyObject] = [String:AnyObject]()
        //print(message.body.event)
        if(message.name == adapterName) {
            //            print("Entra en el if")
       
                let dict = message.body as! [String: AnyObject]
                let event = dict["event"] as! String
                if((dict["extra"]) != nil) {
                    //                print("Entra en el if extra")
                    data = dict["extra"] as! [String: AnyObject]
                                   print(data)
                
                print(event)
        
            }
            
            
            let upevent = event.replacingOccurrences(of: "nbe-", with: "").uppercased()
            switch upevent {
            case "STARTVIBRATION":
                if(data["pattern"] != nil) {
                    let pattern = data["pattern"] as! String
                    sendMessageToWebView(evento:  utils.startVibration(pattern: pattern, command: event))
                }
                break
            case "STATUSBARCOLOR":
                 let color = data["color"] as! String
                 sendMessageToWebView(evento:utils.statusBarColor(color:color, command: event))
                 break
            case "STATUSBATTERY":
                 sendMessageToWebView(evento:utils.statusBattery(command: event))
                 break
            case "GETDARKMODE":
                sendMessageToWebView(evento:utils.getDarkMode(command: event))
                break
            case "COPYCLIPBOARD":
                let text = data["textToCopy"] as! String
                sendMessageToWebView(evento: utils.copyClipboard(text: text, command: event))
                break
            case "GETDEVICEMODEL":
                sendMessageToWebView(evento: utils.getDeviceModel(command: event))
                break
            case "OPERATIVESYSTEM":
                sendMessageToWebView(evento: utils.getOperativeSystem(command: event))
                break
            case "GENERATEPUBLICKEY":
                sendMessageToWebView(evento: utils.generateKeyPair(command: event))
                break
            case "SIGNTOKEN":
                let text = data["token"] as! String
                sendMessageToWebView(evento: utils.signText(text: text, command: event))
                break
            case "GETDEVICEUUID":
                sendMessageToWebView(evento:utils.getUuid(command: event))
                break
            case "GETAPPVERSION":
                sendMessageToWebView(evento:utils.getAppVersion(command: event))
                  break
            case "CLEARCACHE":
                sendMessageToWebView(evento:utils.clearCache(command: event))
                break
            case "DOWNLOADPDF":
                let url = data["url"] as! String
                utils.downloadPdf(pdfToDownload: url, command: event)
                let evento = SendPlugin().sendPluginSuccess(response:"Ok", command: event)
                self.sendMessageToWebView(evento: evento)
                break
            case "HASNOTCH":
                sendMessageToWebView(evento:utils.hasNotch(command: event))
                break
            case "NETWORKSTATELISTENER":
                sendMessageToWebView(evento:utils.listenConnection(command: event))
                break
            case "ISDEVICEROOTED":
                sendMessageToWebView(evento:utils.isDeviceRooted(command: event))
                break
                
            case "PICKCONTACT":
                let picker = CNContactPickerViewController()
                picker.delegate = self
                self.present(picker, animated: true, completion: nil)
                break
            case "OPENAPP":
                let nombreIos = data["nombreIos"] as! String
                let idIos = data["idIos"] as! String
                sendMessageToWebView(evento:appopener.checkOpenAppEvent(idIos: idIos, nombreIos: nombreIos, command: event))
                break
            case "OPENPROFILEAPP":
                let nombreIos = data["nombreIos"] as! String
                let idIos = data["idIos"] as! String
                let userProfile = data["userProfile"] as! String
                sendMessageToWebView(evento:appopener.checkOpenAppProfileEvent(idIos: idIos, nombreIos: nombreIos, userProfile: userProfile, command: event))
                break
            case "SHARE":
               let text = data["text"] as! String
               sendMessageToWebView(evento:appopener.shareToApp(text: text, command: event))
               break
            case "CHECKBIOMETRICAVAILABILITY":
                sendMessageToWebView(evento: biometrics.checkAvailability(command: event))
                break
            case "INITBIOMETRICIDENTIFICATION":
                biometrics.startIdentification{(success) in
                    print(success)
                    self.sendMessageToWebView(evento: success)
                }
                break
            case "UPDATEBIOMETRICFLAG":
                let biometricValue = data["biometricValue"] as! String
                sendMessageToWebView(evento: biometrics.updateFlag(text: biometricValue, command: event))
                break
            case "GETBIOMETRICFLAG":
                sendMessageToWebView(evento: biometrics.getFlag(command: event))
                break
                
            case "UPDATEPUSHFLAG":
                let flag = data["flag"] as! String
                sendMessageToWebView(evento: push.updateFlag(text: flag, command: event))
                break
                
            case "GETPUSHFLAG":
                sendMessageToWebView(evento: push.getFlag(command: event))
                break
                
                
            case "GETIDPUSH":
                sendMessageToWebView(evento: push.getID(command: event))
                break
                
            case "ISPUSHNOTIFICATIONENABLED":
                sendMessageToWebView(evento: push.isPushNotificationEnabled(command: event))
                break
         
                
            case "GENERATEIDPUSH":
                push.turnListenerOn{(success) in
                    print(success)
                    self.sendMessageToWebView(evento: success)
                }
                
                break
                
            case "INITPUSHLISTENER":
                sendMessageToWebView(evento: push.onMessageReceived(command: event))
                break
            case "CLOSEAPP":
                UIControl().sendAction(#selector(NSXPCConnection.suspend), to: UIApplication.shared, for: nil)
                break
            case "KILLAPP":
                exit(0)
                break
                
            default:
                break;
            }
        } else {
            print("No es un mensaje de la web de nbe")
        }
    }
    
    func sendPushIdToWebview(evento: Event) {
        print("En el sendPushIdToWebbview")
        sendMessageToWebView(evento: evento)
    }
    
    // Función que ejecuta una función de la web
    func sendMessageToWebView(evento: Any) {
        let obj = JSONSerializer.toJson(evento)
        print ("Return: " + obj)
        let jsToCode = "webviewListener(" + obj + ")"
        print ("jsToCode: " + jsToCode)
        
        DispatchQueue.main.async {
            self.webView.evaluateJavaScript(jsToCode) { (resp, err) in
                //print("receiveValue: ", resp)
            }
        }
    }
    

    
    
    func updateUserInterface() {
        print("Status:", Network.reachability.status)
        print("Reachable:", Network.reachability.isReachable)
        print("Wifi:", Network.reachability.isReachableViaWiFi)
        
        sendMessageToWebView(evento:utils.listenConnection(command: "nbe-networkStateListener"))
    }
    @objc func lowBattery(notification: Notification) {
         sendMessageToWebView(evento:utils.statusBattery(command: "native-statusBattery"))
    }
    
    @objc func proximityStateDidChange(notification: Notification) {
        let data = DataResponse()
        data.response = notification.description
        let extra = Extra(codigoRetorno: 1, data: data)
        let event = Event(event: "native-proximityState", extra: extra)
        sendMessageToWebView(evento:event)
    }
    func messageReceived(notification: Notification) {
        
//        RECEIVE MESSAGE FROM FIREBASE
         if let messageID = notification.userInfo?[gcmMessageIDKey] {
                  print("Message ID WebView: \(messageID)")
            if let aps = notification.userInfo?["aps"] as? NSDictionary {
                if let alert = aps["alert"] as? NSDictionary {
                        let body = alert["body"]
                        let title = alert["title"]
                        let command =  "native-onPushReceived"
                        let data = DataResponse()
                        data.response = "{\"id\":\"\(messageID)\",\"title\":\"\(title!)\",\"msg\":\"\(body!)\"}"
                        let extra = Extra(codigoRetorno: 1, data: data)
                        let event = Event(event: command, extra: extra)
                        print("desde el WebView")
                        sendMessageToWebView(evento:event)
                   
                }
            }
        }
//        RECEIVE MESSAGE FROM CAJARURAL
        
            print(notification.userInfo!)
        
            if let msg = notification.userInfo?["msg"] {
                let title = "Aviso"
                let fecha = notification.userInfo?["fecha"]
                let tipo = notification.userInfo?["tipo"]
                let id = notification.userInfo?["id"]
                let usr = notification.userInfo?["usr"]
                let command =  "native-onPushReceived"
                let data = DataResponse()
                data.response = "{\"id\":\"\(id!)\",\"title\":\"\(title)\",\"msg\":\"\(msg)\",\"fecha\":\"\(fecha!)\",\"tipo\":\"\(tipo!)\",\"usr\":\"\(usr!)\"}"
                let extra = Extra(codigoRetorno: 1, data: data)
                let event = Event(event: command, extra: extra)
                print("desde el WebView")
                sendMessageToWebView(evento:event)
            }
      }
  
    
    @objc func statusManager(_ notification: Notification) {
        updateUserInterface()
    }
    
    @objc func onChangeStatus(_ notification: Notification) {
        
      
          let data = DataResponse()
          data.response = notification.description
          let extra = Extra(codigoRetorno: 1, data: data)
          let event = Event(event: "native-listenForeground", extra: extra)
          sendMessageToWebView(evento:event)
         
     }
    
  
    
    @objc func didReceiveData(_ notification: Notification) {
        messageReceived(notification: notification)
      }
    
    
    
    @objc func checkIfIsAccessedFromChecklist(_ notification: Notification){
           print("change status")
           if let object = notification.object as? Bool{
               print("Object status: \(object)")
                  let data = DataResponse()
                  data.response = object.description
                  let extra = Extra(codigoRetorno: 1, data: data)
                  let event = Event(event: "native-listenForeground", extra: extra)
                  sendMessageToWebView(evento:event)
               
           }
       }
    
    // A selected contact is returned with this method.
       func contactPicker(_ picker: CNContactPickerViewController, didSelect contact: CNContact) {
        let contactPhone = ((contact.phoneNumbers.first?.value)?.stringValue)
            let contactName = CNContactFormatter.string(from: contact, style: .fullName) ?? "No Name"

        if(contactPhone == nil){
            let data = DataResponse()
            data.response = "{code:265, description:\"El contacto seleccionado no tiene número de teléfono.\"}"
            let extra = Extra(codigoRetorno: 1, data: data)
            let event = Event(event: "nbe-pickContactReturn", extra: extra)
            sendMessageToWebView(evento:event)
            
        }else{
            let data = DataResponse()
            data.response = "{name:\"\(contactName)\",phone:\"\(contactPhone!)\"}"
            let extra = Extra(codigoRetorno: 0, data: data)
            let event = Event(event: "nbe-pickContactReturn", extra: extra)
            sendMessageToWebView(evento:event)
        }
       }
    
    
    
    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        // Trait collection has already changed
    }

    override func willTransition(to newCollection: UITraitCollection, with coordinator: UIViewControllerTransitionCoordinator) {
        // Trait collection will change. Use this one so you know what the state is changing to.
    }
    
    
    
    
}




