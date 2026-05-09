package com.nihit.craft_connect.constants;


import com.nihit.craft_connect.exception.AppException;

public final class DatabaseConstant {
    private DatabaseConstant() {
        throw new AppException(NOT_INSTANTIABLE);
    }

    private static final String NOT_INSTANTIABLE = "Util Class. Unable to instantiate";
    public static final String UNIQUE = "unique_";
    public static final String FOREIGN_KEY = "fk_";
}
