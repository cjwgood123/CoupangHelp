package com.coupang.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleDataAccessException(DataAccessException e, Model model) {
        logger.error("Database access error: ", e);
        
        Throwable rootCause = e.getRootCause();
        String errorMessage = "데이터베이스 연결 오류가 발생했습니다.";
        
        if (rootCause instanceof SQLException) {
            SQLException sqlException = (SQLException) rootCause;
            errorMessage = "데이터베이스 오류: " + sqlException.getMessage();
            logger.error("SQL Error Code: {}, SQL State: {}", 
                sqlException.getErrorCode(), sqlException.getSQLState());
        }
        
        model.addAttribute("error", errorMessage);
        model.addAttribute("message", "데이터베이스에 연결할 수 없습니다. 서버 관리자에게 문의하세요.");
        return "error";
    }

    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleSQLException(SQLException e, Model model) {
        logger.error("SQL error: ", e);
        model.addAttribute("error", "SQL 오류: " + e.getMessage());
        model.addAttribute("message", "데이터베이스 쿼리 실행 중 오류가 발생했습니다.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        logger.error("Unexpected error: ", e);
        model.addAttribute("error", "예상치 못한 오류가 발생했습니다.");
        model.addAttribute("message", e.getMessage());
        return "error";
    }
}






