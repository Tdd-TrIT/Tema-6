CREATE TABLE IF NOT EXISTS CUSTOMER ( 
	ID INT GENERATED BY DEFAULT AS IDENTITY(START WITH 1000) PRIMARY KEY,
	LEGAL_IDENTIFIER VARCHAR(50) NOT NULL UNIQUE,
	NAME VARCHAR(50) NULL,
	LASTNAME VARCHAR(50) NULL,
	EMAIL VARCHAR(250) NULL,
	PHONE VARCHAR(25) NULL
)
;