//
//  Biometrics.swift
//  NuevaBanca-ios
//
//  Created by Diego Perez Salas RSI on 11/03/2020.
//  Copyright © 2020 Daniel Parra Crespo. All rights reserved.
//

import Foundation
import LocalAuthentication
import UIKit

 class Biometric{
    // Código y descripción de los errores de este plugin
    public enum ErrorCodes :String {
        case BIOMETRICS_TYPE_ERROR = "{code: 305, description: Error al obtener el tipo de identificación biométrica.}"
        case BIOMETRICS_NOT_AVAILABLE = "{code: 306, description: El reconocimiento biométrico no está disponible.}"
        case BIOMETRICS_OPENMODAL_ERROR = "{code: 316, description: Ha habido un error al iniciar el reconocimiento biométrico.}"
        case BIOMETRICS_UPDATE_DATA = "{code: 320, description: Ha habido un error al actualizar la BBDD.}"
        case BIOMETRICS_INSERT_DATA = "{code: 321, description: Ha habido un error al hacer un insert en la BBDD.}"
        case BIOMETRICS_GET_DATA = "{code: 322, description: Ha habido un error al obtener los datos de la BBDD.}"
        case BIOMETRICS_OPEN_DATABASE = "{code: 323, description: Ha habido un error al iniciar la BBDD.}"
        case BIOMETRICS_CREATE_TABLE = "{code: 324, description: Ha habido un error al crear la tabla en BBDD.}"
    }
    
    var BIOMETRICS_FLAG_KEY: String?
    
    init() {
         BIOMETRICS_FLAG_KEY = "biometrics-flag-key"
    }
    
    //3.3.1 NBE-CHECKBIOMETRICAVAILABILITY
    // Para comprobar si el dispositivo acepta el uso de reconocimiento biométrico
   func checkAvailability(command: String) ->Event{
        let authenticationContext = LAContext();
        var error:NSError?;
        let policy:LAPolicy = .deviceOwnerAuthenticationWithBiometrics;
        
        let available = authenticationContext.canEvaluatePolicy(policy, error: &error);
        
    if(error != nil && error?.code == LAError.biometryNotAvailable.rawValue){
       
            return SendPlugin().sendPluginError(code: 306, description: "El reconocimiento biométrico no está disponible", command: command)
          
        } else {
      
            return SendPlugin().sendPluginSuccess(response: String(available), command: command)
           
        }
    }

  
    
    func getFlag(command: String) ->Event{
        let preferences = UserDefaults.standard
        var flag = preferences.string(forKey: BIOMETRICS_FLAG_KEY!)
        if(flag == nil){
            flag = "false"
        }
         return SendPlugin().sendPluginSuccess(response: String(flag!), command: command)
    }
    
    func updateFlag(text:String, command: String)->Event{
        writeStringToUserDefaults(value: text as String, key: BIOMETRICS_FLAG_KEY!)
        return SendPlugin().sendPluginSuccess(response: "OK", command: command)
    }

    func writeToUserDefaults(flag: Bool, key: String) {
        let preferences = UserDefaults.standard
        preferences.set(flag, forKey: key)
    }
    
    func writeStringToUserDefaults(value: String, key: String) {
        let preferences = UserDefaults.standard
        preferences.set(value, forKey: key)
    }
    

    
    // Para iniciar la identificación biométrica
    
    func startIdentification(completion: @escaping ((Event) -> ())){
        
        let authenticationContext = LAContext()
        let reason = "Authentication"
        let policy:LAPolicy = .deviceOwnerAuthentication
        authenticationContext.evaluatePolicy(
                 policy,
                 localizedReason: reason,
                 reply: { [] (success, error) -> Void in
                     if( success ){
                        print(reason)
                        completion(SendPlugin().sendPluginSuccess(response: "success", command: "nbe-initBiometricIdentification"))
                     }else {
                         // Check if there is an error
                         print("En el else de la huella")
                         if error != nil {
                             print(error?.localizedDescription as Any)
                            if (error?.localizedDescription == "Canceled by user."){
                                completion(SendPlugin().sendPluginSuccess(response: "canceled", command: "nbe-initBiometricIdentification"))
                            }else{
                                completion(SendPlugin().sendPluginSuccess(response: "failed", command: "nbe-initBiometricIdentification"))
                            }
                            
                         }
                  
                     }
             })




    }
    

    
   
    
}
