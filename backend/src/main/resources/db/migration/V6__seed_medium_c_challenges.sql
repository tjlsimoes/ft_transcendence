INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'ft_atoi',
    'Assignment name: ft_atoi\nExpected files: ft_atoi.c\nAllowed functions: None\n\nWrite a function that converts the string argument str to an int and returns it.\nBehavior should match the standard atoi(const char *str).\n\nFunction prototype:\nint ft_atoi(const char *str);',
    'MEDIUM',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'MEDIUM'),
    '[{"input":"ft_atoi(\"42\")","expected_output":"42","is_hidden":false},{"input":"ft_atoi(\"   -214\")","expected_output":"-214","is_hidden":true},{"input":"ft_atoi(\"+00123abc\")","expected_output":"123","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'ft_atoi'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'snake_to_camel',
    'Assignment name: snake_to_camel\nExpected files: snake_to_camel.c\nAllowed functions: malloc, free, realloc, write\n\nWrite a program that takes a single snake_case string and converts it to lowerCamelCase.\nIf argc != 2, print a newline only.',
    'MEDIUM',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'MEDIUM'),
    '[{"input":"./snake_to_camel \"here_is_a_snake_case_word\"","expected_output":"hereIsASnakeCaseWord\\n","is_hidden":false},{"input":"./snake_to_camel \"hello_world\"","expected_output":"helloWorld\\n","is_hidden":true},{"input":"./snake_to_camel","expected_output":"\\n","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'snake_to_camel'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'union',
    'Assignment name: union\nExpected files: union.c\nAllowed functions: write\n\nWrite a program that takes two strings and prints, without duplicates, the characters\nthat appear in either string, in the order they appear in argv, followed by newline.\nIf argc != 3, print a newline only.',
    'MEDIUM',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'MEDIUM'),
    '[{"input":"./union zpadinton \"paqefwtdjetyiytjneytjoeyjnejeyj\"","expected_output":"zpadintoqefwjy\\n","is_hidden":false},{"input":"./union ddf6vewg64f gtwthgdwthdwfteewhrtag6h4ffdhsd","expected_output":"df6vewg4thras\\n","is_hidden":true},{"input":"./union","expected_output":"\\n","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'union'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'do_op',
    'Assignment name: do_op\nExpected files: *.c, *.h\nAllowed functions: atoi, printf, write\n\nWrite a program that takes three strings: int operand, operator (+ - * / %), int operand.\nPrint operation result followed by newline. If argc != 4, print newline only.',
    'MEDIUM',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'MEDIUM'),
    '[{"input":"./do_op \"123\" \"*\" 456","expected_output":"56088\\n","is_hidden":false},{"input":"./do_op \"9828\" \"/\" 234","expected_output":"42\\n","is_hidden":true},{"input":"./do_op \"1\" \"+\" \"-43\"","expected_output":"-42\\n","is_hidden":true},{"input":"./do_op","expected_output":"\\n","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'do_op'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'reverse_bits',
    'Assignment name: reverse_bits\nExpected files: reverse_bits.c\nAllowed functions: None\n\nWrite a function that takes one byte and returns it with bits reversed.\n\nFunction prototype:\nunsigned char reverse_bits(unsigned char octet);',
    'MEDIUM',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'MEDIUM'),
    '[{"input":"reverse_bits(0x26)","expected_output":"0x64","is_hidden":false},{"input":"reverse_bits(0x01)","expected_output":"0x80","is_hidden":true},{"input":"reverse_bits(0x00)","expected_output":"0x00","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'reverse_bits'
);
