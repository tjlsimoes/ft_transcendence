UPDATE challenges
SET test_cases = '[{"input":"42","expected_output":"42","is_hidden":false},{"input":"   -214","expected_output":"-214","is_hidden":true},{"input":"+00123abc","expected_output":"123","is_hidden":true}]'::jsonb
WHERE title = 'ft_atoi';
