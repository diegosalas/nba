//
//  Data.swift
//  NuevaBanca-ios
//
//  Created by Diego Perez Salas RSI on 06/03/2020.
//  Copyright © 2020 Daniel Parra Crespo. All rights reserved.
//

import Foundation
class DataResponse {
    var code: Int?
    var description: String?
    var response: String?
    
    init(code: Int? = nil, description: String? = nil, response: String? = nil) {
        
    }
    func getData(codigoRetorno:Int) -> String {
        let dataArray: String
        if (codigoRetorno == 1){
            dataArray = "response: \(String(describing: self.response))"
        }else{
            dataArray = "code: \(String(describing: self.code!)), description: \(String(describing: self.description!))"
        }
        
        return dataArray
    }
}
