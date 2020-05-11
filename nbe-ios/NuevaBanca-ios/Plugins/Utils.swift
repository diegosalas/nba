//
//  Utils.swift
//  NuevaBanca-ios
//
//  Created by Daniel Parra Crespo on 04/03/2020.
//  Copyright © 2020 Daniel Parra Crespo. All rights reserved.
//  http://arpa/wikidoc/index.php/API_-_Aplicaci%C3%B3n_Nueva_Banca_Electr%C3%B3nica#Plugin_Utils

import AudioToolbox.AudioServices
import Foundation
import CoreFoundation
import Security
import CommonCrypto
import UIKit
import Contacts
import CoreTelephony

class Utils {
    

    var description: String = ""
    var mPrivateKey: SecKey?
    var mPublicKey: SecKey?
    var PRIVATE_KEY_TAG: String = ""
    var PUBLIC_KEY_TAG: String = ""
    var PUSH_KEY_TAG: String = ""
    var pdfURL: URL?
    var docController:UIDocumentInteractionController?
    
    
    //    3.2.1 NBE-STARTVIBRATION
    func startVibration(pattern: String, command:String) -> Event{
        //print("Entra en startVibration: ")
        let patternArray = pattern.split(separator: ",")
        for n in 0...patternArray.count-1 {
            if n%2 == 0 {
                AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
            }
        }
//        let command = command + "Return"
//        let data = DataResponse()
//        let extra = Extra(codigoRetorno: 1, data: data)
//        let event = Event(event: command, extra: extra)
       
        return SendPlugin().sendPluginSuccess(response: "", command: command)
    }
    // 3.2.2 NBE-COPYCLIPBOARD Función que copia un texto al portapapeles
    func copyClipboard(text: String, command: String) ->Event{
//        print("Entra en copyClipboard")
//        print(text)
        UIPasteboard.general.string = text
//        let command = command + "Return"
//        let data = DataResponse()
//        let extra = Extra(codigoRetorno: 1, data: data)
//        let event = Event(event: command, extra: extra)
        return SendPlugin().sendPluginSuccess(response: "", command: command)
    }
    //3.2.7 NBE-GETDEVICEUUID
    // Función que devuelve el modelo del dispositivo
    func getDeviceModel (command: String) -> Event  {
        //print("Entra en getDeviceModel")
        var systemInfo = utsname()
        uname(&systemInfo)
        let machineMirror = Mirror(reflecting: systemInfo.machine)
        let identifier = machineMirror.children.reduce("") { identifier, element in
            guard let value = element.value as? Int8, value != 0 else { return identifier }
            return identifier + String(UnicodeScalar(UInt8(value)))
        }
        
        if (identifier != ""){
            // print("Todo ha ido a la perfección: " + identifier)
            return SendPlugin().sendPluginSuccess(response: identifier, command: command)
        }
        else{
            // print("Ha habido un error al obtener el modelo de dispositivo")
            return SendPlugin().sendPluginError(code: 271, description: "Ha habido un error al obtener el modelo del dispositivo", command: command)
        }
    }
    //    3.2.3 NBE-OPERATIVESYSTEM
    func getOperativeSystem(command: String) -> Event {
        return SendPlugin().sendPluginSuccess(response: "iOS", command:command)
    }
    
    //    3.2.4 NBE-GENERATEPUBLICKEY

    func generateKeyPair(command: String) -> Event {
        
        if #available(iOS 10.0, *) {
            let privateIdentifier = PUSH_KEY_TAG.data(using: .utf8)!
            let publicIdentifier = PUSH_KEY_TAG.data(using: .utf8)!
            deleteSecureKeyPair()
            //let aclObject = SecAccessControlCreateWithFlags(kCFAllocatorDefault,kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,[.privateKeyUsage,.touchIDAny],nil)
            // private key parameters
            let privateKeyParams: [String: AnyObject] = [
                //kSecAttrAccessControl as String:    aclObject as AnyObject, //protect with touch id
                kSecAttrIsPermanent as String:      true as AnyObject,
                kSecAttrApplicationTag as String:   privateIdentifier as AnyObject
            ]
            
            // private key parameters
            let publicKeyParams: [String: AnyObject] = [
                //kSecAttrAccessControl as String:    aclObject as AnyObject, //protect with touch id
                kSecAttrIsPermanent as String:      true as AnyObject,
                kSecAttrApplicationTag as String:   publicIdentifier as AnyObject
            ]
            
            // global parameters for our key generation
            let parameters: [String: AnyObject] = [
                kSecAttrTokenID as String:          kSecAttrTokenIDSecureEnclave,
                kSecAttrKeyType as String:          kSecAttrKeyTypeEC,
                kSecAttrKeySizeInBits as String:    256 as AnyObject,
                kSecPublicKeyAttrs as String:       publicKeyParams as AnyObject,
                kSecPrivateKeyAttrs as String:      privateKeyParams as AnyObject
            ]
            return pairkey_fun(parameters: parameters as CFDictionary, command: command)
            
        } else {
            print("Tiene versión de iOS menor a la 10.0")
            return SendPlugin().sendPluginError(code: 230, description: ErrorCodes.UTILS_PAIR_GENERATOR_VERSION.rawValue, command: command)
            
        }
        
    }
    
    
    func deleteSecureKeyPair() {
        let keyChainIdentifier = PUSH_KEY_TAG.data(using: .utf8)!
        // private query dictionary
        let deleteQuery: [String : Any] = [
            kSecClass as String: kSecClassKey,
            kSecAttrApplicationTag as String: keyChainIdentifier,
        ]
        
        let status = SecItemDelete(deleteQuery as CFDictionary)
        print("En el deleteSecureKeyPair: ", status)
    }
    
    func pairkey_fun(parameters : CFDictionary, command: String) -> Event{
        var publicKey: SecKey?
        var privateKey: SecKey?
        let status = SecKeyGeneratePair(parameters as CFDictionary, &publicKey, &privateKey)
        if status == noErr && publicKey != nil && privateKey != nil {
            // print("Key pair generated OK")
            //print("El private es: ", privateKey as Any)
            mPrivateKey = privateKey
            mPublicKey = publicKey
            return external(publicKey: publicKey!, command: command)
        }else{
            return SendPlugin().sendPluginError(code: 232, description: ErrorCodes.UTILS_PAIR_GENERATOR_VERSION.rawValue,command: command)
        }
        
    }
    
    
    
    func external(publicKey: SecKey, command: String)->Event{
        var error: Unmanaged<CFError>?
        if let cfData = SecKeyCopyExternalRepresentation(publicKey, &error) {
            
            let x509Header: Data = Data(_: [UInt8]([48, 89, 48, 19, 6, 7, 42, 134, 72, 206, 61, 2, 1, 6, 8, 42, 134, 72, 206, 61, 3, 1, 7, 3, 66, 0]))
            var pubkeyWraped = x509Header
            pubkeyWraped.append(cfData as Data)
            let formatedPublicKey = pubkeyWraped.base64EncodedString()
            //print(formatedPublicKey)
            return SendPlugin().sendPluginSuccess(response: formatedPublicKey, command: command)
            
        }else{
            return SendPlugin().sendPluginError(code: 232, description: ErrorCodes.UTILS_PAIR_GENERATOR_VERSION.rawValue,command: command)
        }
    }
    
    //    3.2.6 NBE-SIGNTOKEN
    
    func signText( text: String , command: String) -> Event{
        let messageData = text.data(using: String.Encoding.utf8)
        let pk = getPrivateKeyReference() //mPrivateKey//getPrivateKeyReference() //getStoredPrivateKey()
        //print(pk as Any)
        if pk != nil {
            if #available(iOS 10.0, *) {
                let signData = SecKeyCreateSignature(pk!, SecKeyAlgorithm.ecdsaSignatureMessageX962SHA256, messageData! as CFData, nil)
                let signedData = signData! as Data
                let signedString = signedData.base64EncodedString(options: [])
                verifySignature(message: signedString, textoSin: text)
                return SendPlugin().sendPluginSuccess(response: signedString, command: command)
            } else {
                return SendPlugin().sendPluginError(code: 237, description: "La versión del dispositivo es inferior a la versión 10.0 y no puede ejecutar la función.", command: command)
            }
        }else{
            return SendPlugin().sendPluginError(code: 238, description: "No hay ninguna clave privada para firmar el texto" , command: command)
        }
    }
    
    func getPrivateKeyReference() -> SecKey? {
        //print("El getPrivateKeyReference")
        let keyChainIdentifier = PUSH_KEY_TAG.data(using: .utf8)!
        let parameters: [String : Any] = [
            kSecClass as String:                kSecClassKey,
            kSecAttrKeyClass as String:         kSecAttrKeyClassPrivate,
            kSecAttrApplicationTag as String:   keyChainIdentifier,
            kSecReturnRef as String:            true
        ]
        var ref: AnyObject?
        let status = SecItemCopyMatching(parameters as CFDictionary, &ref)
        if status == errSecSuccess {
            return (ref as! SecKey)
        } else {
            return nil
        }
    }
    
    // Función que verifica si el texto firmado concuerda con la clave pública
    func verifySignature(message: String, textoSin: String) {
        //print("Entra en verifySignature")
        let text = textoSin
        let messageData = text.data(using: String.Encoding.utf8)
        let signatureData = Data(base64Encoded: message, options: [])
        
        if #available(iOS 10.0, *) {
            if(mPublicKey != nil){
                let verify = SecKeyVerifySignature(
                    mPublicKey!,
                    SecKeyAlgorithm.ecdsaSignatureMessageX962SHA256,
                    messageData! as CFData,
                    signatureData! as CFData,
                    nil)
                print(verify)
                
            }
        } else {
            // Fallback on earlier versions
        }
    }
    
    
    //    3.2.7 NBE-GETDEVICEUUID
    func getUuid (command: String) -> Event {
        
        let preferences = UserDefaults.standard
        let tag = "nba-idDispositivo"
        var uuid:String = ""
        let algo = preferences.object(forKey: tag)
        if algo == nil {
            uuid = UUID().uuidString
            preferences.set(uuid, forKey: tag)
            let didSave = preferences.synchronize()
            if !didSave {
                return SendPlugin().sendPluginError(code: 251, description: "Ha habido un error al obtener el UUID del dispositivo" , command: command)
            }
        } else {
            uuid = preferences.string(forKey: tag)!
        }
        
        return SendPlugin().sendPluginSuccess(response: uuid, command:command)
    }
    
    //    3.2.8 NBE-GETAPPVERSION
    func getAppVersion (command: String) -> Event {
        let preferences = UserDefaults.standard
        let tag = "nba-getAppVersion"
        var appVersion:String = ""
        appVersion = (Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String)!
        preferences.set(appVersion, forKey: tag)
        let didSave = preferences.synchronize()
        if !didSave {
            return SendPlugin().sendPluginError(code: 251, description: "Ha habido un error al obtener la Version de la aplicación" , command: command)
        }
        return SendPlugin().sendPluginSuccess(response: appVersion, command:command)
    }
    
    //    3.2.9 NBE-CLEARCACHE
    func clearCache (command: String) -> Event {
        URLCache.shared.removeAllCachedResponses()
        return SendPlugin().sendPluginSuccess(response: "Cache cleared!", command:command)
    }
    
    
    //     3.2.8 NBE-DOWNLOADPDF
    // Función para descargar pdf's en carpeta temporal
    func downloadPdf(pdfToDownload: String, command: String)  {

        guard let url = URL(string: pdfToDownload) else { return }
        
        let urlSession = URLSession(configuration: .default, delegate: self as? URLSessionDelegate, delegateQueue: OperationQueue())
        
        let downloadTask = urlSession.downloadTask(with: url)
        downloadTask.resume()
        
        return showPdf(pdfUrl: url, command: command)
    }
    
    // Función para visualizar el pdf
    public func showPdf(pdfUrl: URL, command:String) {
        do {
            let pdf: String = pdfUrl.path
            let fileNameArray = pdf.components(separatedBy: "/")
            let fileName = fileNameArray[fileNameArray.count-1]
            let docURL = try FileManager.default.url(for: .cachesDirectory, in: .userDomainMask, appropriateFor: nil, create: false)
            let contents = try FileManager.default.contentsOfDirectory(at: docURL, includingPropertiesForKeys: [.fileResourceTypeKey], options: .skipsHiddenFiles)
            
            for url in contents {
                if url.description.contains("\(fileName)") {
                    // its your file! do what you want with it!
                    self.docController = UIDocumentInteractionController(url: pdfUrl)
                    self.docController!.delegate = self as? UIDocumentInteractionControllerDelegate
                    self.docController!.presentPreview(animated: true)
                }else{
                    
                    UIApplication.shared.open(pdfUrl)
                    
                }
            }
            
    
        } catch {
       
       
        }
    }
    
    //    3.2.10 NBE-HASNOTCH
    
    func hasNotch(command: String) -> Event {
        //print("Entra en hasNotch")
        if #available(iOS 11.0, *) {
            if let window = UIApplication.shared.windows.first(where: { $0.isKeyWindow }) {
                let bottom = window.safeAreaInsets.bottom
                return SendPlugin().sendPluginSuccess(response: bottom > 0 ? "true" : "false", command: command)
            }else{
                return SendPlugin().sendPluginError(code: 281, description: "Ha habido un error al comprobar si el dispositivo tiene notch." , command: command)
            }
            
        } else {
            // Fallback on earlier versions
            //print("Version menor a ios 11, no tienen notch")
            return SendPlugin().sendPluginSuccess(response:"false", command: command)
        }
    }
    
    //    3.2.12 NBE-ISDEVICEROOTED
    
    func isDeviceRooted(command: String) ->Event{
        //        print("Entra en isDeviceRooted")
        if TARGET_IPHONE_SIMULATOR != 1 {
            //            print("Entra en isDeviceRooted if")
            // Check 1 : existence of files that are common for jailbroken devices
            if FileManager.default.fileExists(atPath: "/Applications/Cydia.app")
                || FileManager.default.fileExists(atPath: "/Library/MobileSubstrate/MobileSubstrate.dylib")
                || FileManager.default.fileExists(atPath: "/bin/bash")
                || FileManager.default.fileExists(atPath: "/usr/sbin/sshd")
                || FileManager.default.fileExists(atPath: "/etc/apt")
                || FileManager.default.fileExists(atPath: "/private/var/lib/apt/")
                || UIApplication.shared.canOpenURL(URL(string:"cydia://package/com.rsi.nba")!) {
                
                //                    sendPluginSuccess(text: "true", command: command)
                return SendPlugin().sendPluginSuccess(response:"true", command: command)
            }
            // Check 2 : Reading and writing in system directories (sandbox violation)
            let stringToWrite = "Jailbreak Test"
            do {
                //                print("Entra en isDeviceRooted try")
                try stringToWrite.write(toFile:"/private/JailbreakTest.txt", atomically:true, encoding:String.Encoding.utf8)
                //Device is jailbroken
                //                sendPluginSuccess(text: "true", command: command)
                return SendPlugin().sendPluginSuccess(response:"true", command: command)
            } catch {
                //return false
                //                print("Entra en isDeviceRooted catch")
                //                sendPluginSuccess(text: "false", command: command)
                return SendPlugin().sendPluginSuccess(response:"false", command: command)
            }
        } else {
            print("Entra en isDeviceRooted else")
            //return false
            //          sendPluginSuccess(text: "false", command: command)
            return SendPlugin().sendPluginSuccess(response:"false", command: command)
        }
    }
    
    //    3.2.13 NBE-STATUSBATTERY
    
       func statusBattery(command:String) -> Event{
            UIDevice.current.isBatteryMonitoringEnabled = true
            if (batteryLevel < 0.2){
                 return SendPlugin().sendPluginSuccess(response: "LOW", command: command)
            }else{
                let level = "\(Int((batteryLevel) * 100))%"
                return SendPlugin().sendPluginSuccess(response: level, command: command)
            }
        }
    
        var batteryLevel: Float {
                return UIDevice.current.batteryLevel
          }
          var batteryState: UIDevice.BatteryState {
              return UIDevice.current.batteryState
          }
          
          func batteryLevelDidChange (notification: Notification) {
             print("\(Int((batteryLevel) * 100))%")
          }

          @objc func batteryStateDidChange (notification: Notification) {
             switch batteryState {
             case .unplugged, .unknown:
                 print("not charging")
             case .charging, .full:
                 print("charging or full")
             }
          }
    
    // 3.2.14 NBE-GETDARKMODE
    func getDarkMode(command:String) -> Event{
        if #available(iOS 13.0, *) {
            if UITraitCollection.current.userInterfaceStyle == .dark {
               return SendPlugin().sendPluginSuccess(response: "ON", command: command)
            }
            else {
               return SendPlugin().sendPluginSuccess(response: "OFF", command: command)
            }
        }else{
            return SendPlugin().sendPluginSuccess(response: "OFF", command: command)
        }
        
    }
      
    //    3.2.15 NBE-STATUSBARCOLOR
    
    func statusBarColor(color: String, command:String) -> Event{
        let barcolor = hexStringToUIColor(hex: color)
        UIApplication.statusBarBackgroundColor = barcolor
        return SendPlugin().sendPluginSuccess(response:"OK", command: command)
    }
    func hexStringToUIColor (hex:String) -> UIColor {
        var cString:String = hex.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()

        if (cString.hasPrefix("#")) {
            cString.remove(at: cString.startIndex)
        }

        if ((cString.count) != 6) {
            return UIColor.gray
        }

        var rgbValue:UInt64 = 0
        Scanner(string: cString).scanHexInt64(&rgbValue)

        return UIColor(
            red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
            alpha: CGFloat(1.0)
        )
    }
    
 
    
    func listenConnection(command:String) ->Event{
        switch Network.reachability.status {
           case .unreachable:
               return SendPlugin().sendPluginSuccess(response:"None", command: command)
           case .wwan:
            
                   // Queda pendiente agregar  5G - URLLC (Ultra Reliable Low Latency Communications) systems, cuando salga iPhone12
                   // 7/5/2020 ->Diego
            
                   let networkInfo = CTTelephonyNetworkInfo()
                   let networkString = networkInfo.currentRadioAccessTechnology
                  
                   if networkString == CTRadioAccessTechnologyLTE {
                      return SendPlugin().sendPluginSuccess(response:"4G", command: command)
                   }else if networkString == CTRadioAccessTechnologyHSUPA{
                      return SendPlugin().sendPluginSuccess(response:"3G", command: command)
                   }else if networkString == CTRadioAccessTechnologyHSDPA{
                      return SendPlugin().sendPluginSuccess(response:"3G", command: command)
                   }else if networkString == CTRadioAccessTechnologyeHRPD{
                      return SendPlugin().sendPluginSuccess(response:"3G", command: command)
                   }else if networkString == CTRadioAccessTechnologyWCDMA{
                      return SendPlugin().sendPluginSuccess(response:"3G", command: command)
                   }else if networkString == CTRadioAccessTechnologyEdge{
                     return SendPlugin().sendPluginSuccess(response:"2G", command: command)
                   } else if networkString == CTRadioAccessTechnologyGPRS{
                     return SendPlugin().sendPluginSuccess(response:"2G", command: command)
                   }else if networkString == CTRadioAccessTechnologyCDMA1x{
                     return SendPlugin().sendPluginSuccess(response:"2G", command: command)
                   }
                   else {
                    return SendPlugin().sendPluginSuccess(response:"Unavailable", command: command)
                  }
             
           case .wifi:
             return SendPlugin().sendPluginSuccess(response:"Wifi", command: command)
           }
    }
}

extension UIApplication {
    class var statusBarBackgroundColor: UIColor? {
        get {
            return (shared.value(forKey: "statusBar") as? UIView)?.backgroundColor
        } set {
            (shared.value(forKey: "statusBar") as? UIView)?.backgroundColor = newValue
        }
    }
}








