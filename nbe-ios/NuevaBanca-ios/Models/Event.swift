//
//  Event.swift
//  NuevaBanca-ios
//
//  Created by Diego Perez Salas RSI on 06/03/2020.
//  Copyright © 2020 Daniel Parra Crespo. All rights reserved.
//

import Foundation


class Event {
    var event: String
    var extra: Extra
 
    init(event: String, extra: Extra) {
        self.event = event
        self.extra = extra
        
     }
    
    func getEvent() -> String {
        return "event :\(self.event) \n company \(self.extra)"
    }
    
 
   
}


