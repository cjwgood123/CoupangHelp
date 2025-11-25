-- 쿠팡 데이터 분석 성능 개선을 위한 인덱스 생성 스크립트
-- 실행 전에 데이터베이스 백업을 권장합니다.

-- 1. productID 인덱스 (가장 중요한 인덱스)
-- 모든 테이블에 productID 인덱스가 있는지 확인하고 없으면 생성
-- 예시: coupang_products_100_20251120 테이블에 인덱스 생성

-- 2. regidate 인덱스 (날짜 조회 최적화)
-- 날짜 기반 조회 성능 향상

-- 3. 복합 인덱스 (productID + regidate)
-- 상품별 최신 날짜 조회 최적화

-- 주의: 아래 스크립트는 예시입니다. 실제 테이블명에 맞게 수정하세요.

-- 동적으로 모든 테이블에 인덱스 생성하는 방법:
-- 1. 먼저 테이블 목록 조회
-- SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES 
-- WHERE TABLE_SCHEMA = 'CouPangHelp' 
-- AND (TABLE_NAME LIKE 'coupang_products_%' OR TABLE_NAME LIKE 'coupang_satisfied_%');

-- 2. 각 테이블에 대해 인덱스 생성 (productID 인덱스가 없을 경우)
-- 예시:
-- CREATE INDEX IF NOT EXISTS idx_productid ON coupang_products_100_20251120(productID);
-- CREATE INDEX IF NOT EXISTS idx_regidate ON coupang_products_100_20251120(regidate);
-- CREATE INDEX IF NOT EXISTS idx_productid_regidate ON coupang_products_100_20251120(productID, regidate);

-- 자동화 스크립트 (MySQL 8.0 이상)
-- 아래 스크립트를 실행하면 모든 테이블에 인덱스를 자동으로 생성합니다.

DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS create_indexes_for_all_tables()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE table_name VARCHAR(255);
    DECLARE cur CURSOR FOR 
        SELECT TABLE_NAME 
        FROM INFORMATION_SCHEMA.TABLES 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND (TABLE_NAME LIKE 'coupang_products_%' OR TABLE_NAME LIKE 'coupang_satisfied_%');
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO table_name;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- productID 인덱스 생성 (이미 있으면 무시)
        SET @sql = CONCAT('CREATE INDEX IF NOT EXISTS idx_', table_name, '_productid ON ', table_name, '(productID)');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        
        -- regidate 인덱스 생성
        SET @sql = CONCAT('CREATE INDEX IF NOT EXISTS idx_', table_name, '_regidate ON ', table_name, '(regidate)');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        
        -- 복합 인덱스 생성 (productID, regidate)
        SET @sql = CONCAT('CREATE INDEX IF NOT EXISTS idx_', table_name, '_productid_regidate ON ', table_name, '(productID, regidate)');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        
    END LOOP;
    
    CLOSE cur;
END$$

DELIMITER ;

-- 프로시저 실행
CALL create_indexes_for_all_tables();

-- 프로시저 삭제 (선택사항)
-- DROP PROCEDURE IF EXISTS create_indexes_for_all_tables;

-- 인덱스 생성 확인
-- SELECT 
--     TABLE_NAME,
--     INDEX_NAME,
--     COLUMN_NAME,
--     SEQ_IN_INDEX
-- FROM INFORMATION_SCHEMA.STATISTICS
-- WHERE TABLE_SCHEMA = DATABASE()
-- AND TABLE_NAME LIKE 'coupang_products_%'
-- ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;




