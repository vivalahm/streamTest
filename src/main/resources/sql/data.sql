-- DML for inserting sample data

-- Insert sample data into Processor table
INSERT INTO Processor (processor_id, processor_name, partner_code) VALUES
  ('PROC_A', '프로세서 A', 'PARTNER_001'),
  ('PROC_B', '프로세서 B', 'PARTNER_002');

-- Insert sample data into Scheme table
INSERT INTO Scheme (scheme_code, scheme_name) VALUES
  ('NAVER_PAY', '네이버페이'),
  ('TOSS_PAY', '토스페이'),
  ('HYUNDAI_CARD', '현대카드'),
  ('SAMSUNG_CARD', '삼성카드'),
  ('KAKAO_PAY', '카카오페이');

-- Insert sample data into Payment table
INSERT INTO Payment (processor_id, partner_code, payment_type, scheme_code) VALUES
  ('PROC_A', 'PARTNER_001', 'MOBILE', 'NAVER_PAY'),
  ('PROC_A', 'PARTNER_001', 'MOBILE', 'TOSS_PAY'),
  ('PROC_A', 'PARTNER_001', 'CREDIT_CARD', 'HYUNDAI_CARD'),
  ('PROC_A', 'PARTNER_001', 'CREDIT_CARD', 'SAMSUNG_CARD'),
  ('PROC_B', 'PARTNER_002', 'MOBILE', 'KAKAO_PAY');

-- For large-scale testing, you can use the following SQL to generate more data:
/*
-- Generate 1,000 Processors
INSERT INTO Processor (processor_id, processor_name, partner_code)
SELECT CONCAT('P', LPAD(seq,4,'0')), CONCAT('프로세서',seq), CONCAT('PARTNER_',LPAD(seq,3,'0'))
FROM (SELECT @rownum := @rownum + 1 AS seq FROM information_schema.columns a, information_schema.columns b, (SELECT @rownum := 0) r LIMIT 1000) t;

-- Generate 10,000 Schemes
INSERT INTO Scheme (scheme_code, scheme_name)
SELECT CONCAT('SCHEME_',LPAD(seq,5,'0')), CONCAT('스킴',seq)
FROM (SELECT @rownum := @rownum + 1 AS seq FROM information_schema.columns a, information_schema.columns b, (SELECT @rownum := 0) r LIMIT 10000) t;
*/