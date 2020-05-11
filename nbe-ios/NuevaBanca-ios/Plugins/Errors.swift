//
//  Errors.swift
//  NuevaBanca-ios
//
//  Created by Diego Perez Salas RSI on 06/03/2020.
//  Copyright © 2020 Daniel Parra Crespo. All rights reserved.
//

import Foundation


public enum ErrorCodes :String {
    case APPOPENER_MINIMUM_VERSION = "{code: 103, description: El S.O. tiene que ser mínimo v10.0.}"
    case APPOPENER_OPEN_ERROR = "{code: 104, description: No está permitido abrir otra aplicación.}"
    case APPOPENER_GENERIC_ERROR = "{code:105, description: Ha habido un error durante el proceso.}"
    case APPOPENER_CHECK_APP_NOT_INSTALLED = "{code: 111, description: No tiene la aplicación instalada en el dispositivo.}"
    case UTILS_PAIR_GENERATOR_EXCEPTION = "{code: 231, description:  Ha habido un error al generar el KeyPair.}"
    case UTILS_PAIR_GENERATOR_VERSION = "{code: 232, description: La versión del dispositivo es inferior a la versión 10.0 y no puede ejecutar la función.}"
    case UTILS_UUID_GENERATOR = "{code: 233, description: Ha habido un problema al guardar el uuid en el dispositivo.}"
    case UTILS_ENCRYPTION_NO_KEY = "{code: 234, description: No hay generado ninguna clave para encriptar el texto.}"
    case UTILS_ENCRYPTION_ERROR = "{code: 235, description: Ha habido un error al encriptar el texto.}"
    case UTILS_SIGN_ERROR = "{code: 236, description: Ha habido un error al firmar el texto.}"
    case UTILS_SIGN_TEXT_VERSION = "{code: 237, description: La versión del dispositivo es inferior a la versión 10.0 y no puede ejecutar la función.}"
    case UTILS_SIGN_NO_KEY = "{code: 238, description: No hay ninguna clave privada para firmar el texto.}"
    case UTILS_DEVICE_MODEL = "{code: 239, description: Ha habido un problema al obtener el modelo del dispositivo.}"
    case UTILS_HAS_NOTCH = "{code: 240, description: Ha habido un error al comprobar si el dispositivo tiene notch.}"
    case UTILS_DOWNLOAD_PDF = "{code: 241, description: Ha habido un error descargar el pdf.}"
    case UTILS_SHOW_PDF = "{code: 242, description: Ha habido un error mostrar el pdf.}"
    case UTILS_APP_VERSION = "{code: 243, description: Ha habido un error al obtener la versión de la app.}"
    case UTILS_SHARE_DATA = "{code: 244, description: Ha habido un error al compartir datos con una app externa}"
    case BIOMETRICS_TYPE_ERROR = "{code: 305, description: Error al obtener el tipo de identificación biométrica.}"
    case BIOMETRICS_NOT_AVAILABLE = "{code: 306, description: El reconocimiento biométrico no está disponible.}"
    case BIOMETRICS_OPENMODAL_ERROR = "{code: 316, description: Ha habido un error al iniciar el reconocimiento biométrico.}"
    case BIOMETRICS_UPDATE_DATA = "{code: 320, description: Ha habido un error al actualizar la BBDD.}"
    case BIOMETRICS_INSERT_DATA = "{code: 321, description: Ha habido un error al hacer un insert en la BBDD.}"
    case BIOMETRICS_GET_DATA = "{code: 322, description: Ha habido un error al obtener los datos de la BBDD.}"
    case BIOMETRICS_OPEN_DATABASE = "{code: 323, description: Ha habido un error al iniciar la BBDD.}"
    case BIOMETRICS_CREATE_TABLE = "{code: 324, description: Ha habido un error al crear la tabla en BBDD.}"
}
