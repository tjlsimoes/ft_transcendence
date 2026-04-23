INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'ft_list_size',
    'Assignment name: ft_list_size\nExpected files: ft_list_size.c, ft_list.h\nAllowed functions: None\n\nWrite a function that returns the number of elements in the linked list passed as argument.\n\nFunction prototype:\nint ft_list_size(t_list *begin_list);\n\nUse this structure in ft_list.h:\ntypedef struct s_list\n{\n    struct s_list *next;\n    void          *data;\n} t_list;',
    'HARD',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'HARD'),
    '[{"input":"ft_list_size(NULL)","expected_output":"0","is_hidden":false},{"input":"list with 1 node","expected_output":"1","is_hidden":true},{"input":"list with 7 nodes","expected_output":"7","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'ft_list_size'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'add_prime_sum',
    'Assignment name: add_prime_sum\nExpected files: add_prime_sum.c\nAllowed functions: write, exit\n\nWrite a program that takes a positive integer and displays the sum of all prime numbers\ninferior or equal to it, followed by newline.\nIf argc != 2 or input is not a positive number, print 0 followed by newline.',
    'HARD',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'HARD'),
    '[{"input":"./add_prime_sum 5","expected_output":"10\\n","is_hidden":false},{"input":"./add_prime_sum 7","expected_output":"17\\n","is_hidden":true},{"input":"./add_prime_sum","expected_output":"0\\n","is_hidden":true},{"input":"./add_prime_sum -3","expected_output":"0\\n","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'add_prime_sum'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'hidenp',
    'Assignment name: hidenp\nExpected files: hidenp.c\nAllowed functions: write\n\nWrite a program that prints 1 followed by newline if the first string is hidden\nin the second string (subsequence in order), otherwise 0 followed by newline.\nIf argc != 3, print newline only.',
    'HARD',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'HARD'),
    '[{"input":"./hidenp \"abc\" \"2altrb53c.sse\"","expected_output":"1\\n","is_hidden":false},{"input":"./hidenp \"fgex.;\" \"tyf34gdgf;ektufjhgdgex.;.;rtjynur6\"","expected_output":"1\\n","is_hidden":true},{"input":"./hidenp \"abc\" \"btarc\"","expected_output":"0\\n","is_hidden":true},{"input":"./hidenp","expected_output":"\\n","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'hidenp'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'str_capitalizer',
    'Assignment name: str_capitalizer\nExpected files: str_capitalizer.c\nAllowed functions: write\n\nWrite a program that capitalizes the first letter of each word for each argument,\nlowercases the rest, and prints each transformed argument followed by newline.\nIf there are no arguments, print newline only.',
    'HARD',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'HARD'),
    '[{"input":"./str_capitalizer \"a FiRSt LiTTlE TESt\"","expected_output":"A First Little Test\\n","is_hidden":false},{"input":"./str_capitalizer \"__SecONd teST A LITtle BiT   Moar comPLEX\"","expected_output":"__second Test A Little Bit   Moar Complex\\n","is_hidden":true},{"input":"./str_capitalizer","expected_output":"\\n","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'str_capitalizer'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'tab_mult',
    'Assignment name: tab_mult\nExpected files: tab_mult.c\nAllowed functions: write\n\nWrite a program that displays the multiplication table from 1 to 9 for the\nprovided strictly positive int argument.\nIf no parameter is provided, print newline only.',
    'HARD',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'HARD'),
    '[{"input":"./tab_mult 9","expected_output":"1 x 9 = 9\\n2 x 9 = 18\\n3 x 9 = 27\\n4 x 9 = 36\\n5 x 9 = 45\\n6 x 9 = 54\\n7 x 9 = 63\\n8 x 9 = 72\\n9 x 9 = 81\\n","is_hidden":false},{"input":"./tab_mult 19","expected_output":"1 x 19 = 19\\n2 x 19 = 38\\n3 x 19 = 57\\n4 x 19 = 76\\n5 x 19 = 95\\n6 x 19 = 114\\n7 x 19 = 133\\n8 x 19 = 152\\n9 x 19 = 171\\n","is_hidden":true},{"input":"./tab_mult","expected_output":"\\n","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'tab_mult'
);
