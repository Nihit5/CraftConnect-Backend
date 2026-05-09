package com.nihit.craft_connect.constants;


import com.nihit.craft_connect.exception.AppException;

public final class MessageConstant {

    public static final String MESSAGE_NOT_FOR_USER = "message.not.for.user";
    public static final String ATTACHMENT_NOT_FOR_USER = "attachment.not.for.user";

    MessageConstant() {
        throw new AppException(NOT_INSTANTIABLE);
    }

    private static final String NOT_INSTANTIABLE = "Util Class. Unable to instantiate";
    public static final String ERROR_MESSAGE_NOT_FOUND = "error.message.not.found";
    public static final String DELETE_FILE_FAILED = "delete.file.failed";
    public static final String STORE_FILE_FAILED = "store.file.failed";
    public static final String CREATE_FILE_FAILED = "create.file.failed";
    public static final String FILE_DOWNLOAD_FAILED = "file.download.failed";
    public static final String FILE_NOT_FOUND = "file.not.found";
    public static final String USER_LIST = "user list";
    public static final String NOT_IMPORTED_YET = "not.imported.yet";
    public static final String BALANCES_ALREADY_SET = "balances.already.set";
}
