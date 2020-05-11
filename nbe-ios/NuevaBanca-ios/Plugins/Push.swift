//
//  Push.swift
//  NuevaBanca-ios
//
//  Created by Diego Perez Salas RSI on 11/03/2020.
//  Copyright © 2020 Daniel Parra Crespo. All rights reserved.
//

import Foundation
import Firebase
import UserNotifications
import UIKit


class Push {
    var PUSH_FLAG_KEY: String?
    var pushIdCommand: String = ""
    private static var staticCommandDelegate:  String?
    private static var notificationCommand: String?
    
  
//    3.4.2 NBE-GETPUSHFLAG
    func getFlag(command: String) ->Event {
        let preferences = UserDefaults.standard
        var flag = preferences.string(forKey: "PUSH_FLAG_KEY")
        if(flag == nil){
            flag = "false"
        }
        return SendPlugin().sendPluginSuccess(response: String(flag!),command: command)
    }
//    3.4.1 NBE-UPDATEPUSHFLAG
    func updateFlag(text: String, command:String)->Event{
        writeStringToUserDefaults(value: text, key: "PUSH_FLAG_KEY")
        return SendPlugin().sendPluginSuccess(response: "", command: command)
    }
    
    func writeToUserDefaults(flag: Bool, key: String) {
        let preferences = UserDefaults.standard
        preferences.set(flag, forKey: key)
    }
    
    func writeStringToUserDefaults(value: String, key: String) {
        let preferences = UserDefaults.standard
        preferences.set(value, forKey: key)
    }
    //    3.4.5 NBE-GETIDPUSH
    func getID(command: String)->Event {
        let preferences = UserDefaults.standard
        let value = preferences.string(forKey: "PUSH_FIREBASE_ID") ?? ""
        
        if  value.isEmpty {
            return SendPlugin().sendPluginSuccess(response: "", command: command) //return SendPlugin().sendPluginError(code: 521, description: "Ha habido un error al obtener el token de push guardado", command: command)
        } else {
            return SendPlugin().sendPluginSuccess(response: value, command: command)
        }
     }
    
    //    3.4.7 NBE-INITPUSHLISTENER
    func onMessageReceived(command: String) ->Event{
        Push.notificationCommand = command
        return SendPlugin().sendPluginSuccess(response: "", command: command)
    }
    
    //    3.4.7 NBE-generateIdPush
    func turnListenerOn(completion: @escaping ((Event) -> ())) {
        
        Messaging.messaging().delegate = self as? MessagingDelegate
        UNUserNotificationCenter.current().delegate = self as? UNUserNotificationCenterDelegate
        
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
    
        UNUserNotificationCenter.current().requestAuthorization(options: authOptions) {(_, error) in
            guard error == nil else {
                return
            }
        }
        
      UIApplication.shared.registerForRemoteNotifications()
        
        

        InstanceID.instanceID().instanceID { (result, error) in
             if let error = error {
               print("Error fetching remote instange ID: \(error)")
               completion(SendPlugin().sendPluginSuccess(response:"KO", command: "nbe-generateIdPush"))
              //   self.sendMessageToWebView(evento: evento)

             } else if let result = result {
                 print("Device Token 2 : \(result.token)")
                 let preferences = UserDefaults.standard
                 preferences.set(result.token, forKey: "PUSH_FIREBASE_ID")
                completion(SendPlugin().sendPluginSuccess(response:result.token, command: "nbe-generateIdPush"))
               //  self.sendMessageToWebView(evento: evento)

              }
             }
    }
    
    func sendPushId(token: String) {
        print("En el sendPushId")
        print(token)
        ViewController().sendPushIdToWebview(evento: SendPlugin().sendPluginSuccess(response: token, command: "nbe-generateIdPush"))
    }

    func isPushNotificationEnabled(command:String)->Event{
        print(String(Push.isPushNotificationEnabled))
        return SendPlugin().sendPluginSuccess(response: String(Push.isPushNotificationEnabled), command: command)
    }
    
    static var isPushNotificationEnabled: Bool {
      guard let settings = UIApplication.shared.currentUserNotificationSettings
        else {
          return false
      }

      return UIApplication.shared.isRegisteredForRemoteNotifications
        && !settings.types.isEmpty
    }
    
  
}

