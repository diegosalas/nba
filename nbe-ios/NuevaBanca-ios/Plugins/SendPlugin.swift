//
//  sendPlugin.swift
//  NuevaBanca-ios
//
//  Created by Diego Perez Salas RSI on 11/03/2020.
//  Copyright © 2020 Daniel Parra Crespo. All rights reserved.
//

import Foundation


class SendPlugin {
    
    
    func sendPluginSuccess(response: String, command: String ) ->Event{
        let command = command + "Return"
        let data = DataResponse()
        if response != "" {data.response = response}
        let extra = Extra(codigoRetorno: 1, data: data)
        let event = Event(event: command, extra: extra)
        return event

    }


    func sendPluginError(code: Int, description: String, command:String)->Event{
        let command = command + "Return"
        let data = DataResponse()
        data.code = code
        data.description = description
        let extra = Extra(codigoRetorno: 0, data: data)
        let event = Event(event: command, extra: extra)
        return event

    }
}
