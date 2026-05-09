package com.nihit.craft_connect.constants;


import com.nihit.craft_connect.exception.AppException;

public final class ErrorConstants {
    public static final String INTERNAL_SERVER_ERROR = "internal.server.error";
    public static final String PASSWORD_SAME = "error.password.same";
    public static final String RETRIEVAL_FAILURE = "failure.retrieval";
    public static final String SUPER_ADMIN_CONTROLLED_MENU_UNASSIGNABLE = "super.admin.menu.unassignable";
    public static final String CLIENT_CONTROLLED_MENU_UNASSIGNABLE = "client.menu.unassignable";
    public static final String ERROR_HANDLER_MAPPING = "error.handler.mapping";

    ErrorConstants() {
        throw new AppException(NOT_INSTANTIABLE);
    }

    private static final String NOT_INSTANTIABLE = "Util Class. Unable to instantiate";
    public static final String ERROR_ALREADY_EXIST = "error.already.exist";
    public static final String ERROR_DOESNOT_EXIST = "error.does.not.exist";
    public static final String MUST_NOT_BE_EMPTY = "must.not.be.empty";
    public static final String LIST_ERROR_DOESNOT_EXIST = "list.error.does.not.exist";
    public static final String INVALID_TASK_HISTORY_TYPE = "invalid.task.history.type";
    public static final String ERROR_CREATE = "error.create";
    public static final String ERROR_FETCH = "error.fetch";
    public static final String ERROR_CHECK = "error.check";
    public static final String ERROR_UPDATE = "error.update";
    public static final String ERROR = "error";
    public static final String ROLE_NOT_SELECTED = "error.role.not.selected";
    public static final String INCORRECT_PASSWORD = "error.incorrect.password";
    public static final String ERROR_EMAIL_VERIFICATION = "error.email.verification";
    public static final String INCORRECT_PASSWORD_MATCH = "incorrect.password.match";
    public static final String INVALID_TOKEN = "invalid.token";
    public static final String INVALID = "invalid";
    public static final String ERROR_TEXT_TOO_LONG = "error.text.too.long";
    public static final String ERROR_DATABASE_ERROR = "error.database.error";
    public static final String ERROR_TYPE_MISMATCH = "error.type.mismatch";
    public static final String ERROR_METHOD_ARGUMENT_MISMATCH = "error.method.argument.mismatch";
    public static final String ERROR_REQUEST_PARAMETER_MISSING = "error.request.parameter.missing";
    public static final String ERROR_REQUEST_PART_MISSING = "error.request.part.missing";
    public static final String FUNCTION_EXCEPTION = "function_exception_";
    public static final String INVALID_MODULE = "invalid.module";
    public static final String DATABASE_ERROR = "database.error";
    public static final String DATABASE_UPDATE_ERROR = "database.update.error";
    public static final String DATABASE_SETUP_ERROR = "database.setup.error";
    public static final String ACCESS_DENIED = "access.denied";
    public static final String COMPARISON_FIELD_NOT_SET = "comparison.field.not.set";
}
