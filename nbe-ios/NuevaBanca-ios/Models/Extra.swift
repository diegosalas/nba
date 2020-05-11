//
//  Extra.swift
//  NuevaBanca-ios
//
//  Created by Diego Perez Salas RSI on 06/03/2020.
//  Copyright © 2020 Daniel Parra Crespo. All rights reserved.
//

import Foundation
class Extra{
    
    var codigoRetorno: Int
    var data : DataResponse
    var userProfile : String?
    var pattern : String?
    var textToCopy: String?
    var token: String?
    var title: String?
    var desc: String?
    var flag: String?
    
    init(nombreIos: String? = nil,
         idIos: String? = nil,
         codigoRetorno: Int,
         userProfile: String? = nil,
         pattern: String? = nil,
         textToCopy: String? = nil,
         token: String? = nil,
         title: String? = nil,
         desc: String? = nil,
         flag: String? = nil,
         data: DataResponse) {
            self.codigoRetorno = codigoRetorno
            self.data = data
        }
        func getData()-> String {
            let extraArray: String
            if self.data.getData(codigoRetorno: self.codigoRetorno) != "response: nil"{
                extraArray = "data:{" + "\(self.data.getData(codigoRetorno: self.codigoRetorno))" + "},"
            }else{
                extraArray = ""
            }
            return  extraArray
        }
}
