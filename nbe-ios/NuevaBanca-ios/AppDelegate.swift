//
//  AppDelegate.swift
//  NuevaBanca-ios
//
//  Created by Daniel Parra Crespo on 20/02/2020.
//  Copyright © 2020 Daniel Parra Crespo. All rights reserved.
//

import UIKit
import Firebase



let gcmMessageIDKey = "gcm.message_id"


extension Notification.Name {
  
     static let didReceiveData = Notification.Name("didReceiveData")
     static let isAccessedFromChecklistVC = Notification.Name(rawValue: "isAccessedFromChecklistVC")
 }


@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    var window: UIWindow?

    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        FirebaseApp.configure()
        UNUserNotificationCenter.current().delegate = self

        
        do {
                    try Network.reachability = Reachability(hostname: "www.google.com")
                   }
                   catch {
                       switch error as? Network.Error {
                       case let .failedToCreateWith(hostname)?:
                           print("Network error:\nFailed to create reachability object With host named:", hostname)
                       case let .failedToInitializeWith(address)?:
                           print("Network error:\nFailed to initialize reachability object With address:", address)
                       case .failedToSetCallout?:
                           print("Network error:\nFailed to set callout")
                       case .failedToSetDispatchQueue?:
                           print("Network error:\nFailed to set DispatchQueue")
                       case .none:
                           print(error)
                       }
                   }
     
        
        return true
    }
    
    func applicationDidEnterBackground(_ application: UIApplication  ) {
        let isAccessedFromChecklist = false
        NotificationCenter.default.post(name: .isAccessedFromChecklistVC, object: isAccessedFromChecklist)
        print("background")
        
    }
    
   
 
    func applicationWillEnterForeground(_ application: UIApplication) {
        let isAccessedFromChecklist = true
        NotificationCenter.default.post(name: .isAccessedFromChecklistVC, object: isAccessedFromChecklist)
         print("foreground")
    }
   
    
    
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let tokenParts = deviceToken.map { data in String(format: "%02.2hhx", data) }
        let token = tokenParts.joined()
        print("Device Token: \(token)")
//        push.sendPushId(token: token)
    }

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
      print("Failed to register: \(error)")
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any]) {
      if let messageID = userInfo[gcmMessageIDKey] {
        print("Message ID 1: \(messageID)")
      }
        print(userInfo)
        NotificationCenter.default.post(name: .didReceiveData, object: nil, userInfo: userInfo)
    }
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any],
                     fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
      if let messageID = userInfo[gcmMessageIDKey] {
        print("Message ID 2: \(messageID)")
       }
      print(userInfo)
      NotificationCenter.default.post(name: .didReceiveData, object: nil, userInfo: userInfo)
      completionHandler(UIBackgroundFetchResult.newData)
    }
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void)
    {
        print(notification)

        completionHandler([.alert, .badge, .sound])
    }

    
}



