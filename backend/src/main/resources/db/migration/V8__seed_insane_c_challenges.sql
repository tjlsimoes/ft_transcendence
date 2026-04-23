INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'flood_fill',
    'Assignment name: flood_fill\nExpected files: *.c, *.h\nAllowed functions: None\n\nWrite a function that fills a zone in a 2D char array from a start point with F.\nThe zone is connected only horizontally and vertically and uses the same source character.\nNo diagonal fill.\n\nFunction prototype:\nvoid flood_fill(char **tab, t_point size, t_point begin);\n\nt_point:\ntypedef struct s_point\n{\n    int x;\n    int y;\n} t_point;',
    'INSANE',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'INSANE'),
    '[{"input":"size={8,5}, begin={7,4}, zone=[11111111;10001001;10010001;10110001;11100001]","expected_output":"[FFFFFFFF;F000F00F;F00F000F;F0FF000F;FFF0000F]","is_hidden":false},{"input":"begin on isolated cell","expected_output":"only that connected component replaced by F","is_hidden":true},{"input":"begin out of bounds","expected_output":"zone unchanged","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'flood_fill'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'ft_split',
    'Assignment name: ft_split\nExpected files: ft_split.c\nAllowed functions: malloc\n\nWrite a function that splits a string into words and returns a NULL terminated array.\nWords are delimited by spaces, tabs, new lines, or string boundaries.\n\nFunction prototype:\nchar **ft_split(char *str);',
    'INSANE',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'INSANE'),
    '[{"input":"ft_split(\"   Hello\\t42\\nLisbon   \")","expected_output":"[Hello,42,Lisbon]","is_hidden":false},{"input":"ft_split(\"\\n\\t   \")","expected_output":"[]","is_hidden":true},{"input":"ft_split(\"single\")","expected_output":"[single]","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'ft_split'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'ft_itoa',
    'Assignment name: ft_itoa\nExpected files: ft_itoa.c\nAllowed functions: malloc\n\nWrite a function that converts an int to a null terminated allocated string.\n\nFunction prototype:\nchar *ft_itoa(int nbr);',
    'INSANE',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'INSANE'),
    '[{"input":"ft_itoa(0)","expected_output":"0","is_hidden":false},{"input":"ft_itoa(-42)","expected_output":"-42","is_hidden":true},{"input":"ft_itoa(-2147483648)","expected_output":"-2147483648","is_hidden":true},{"input":"ft_itoa(2147483647)","expected_output":"2147483647","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'ft_itoa'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'rostring',
    'Assignment name: rostring\nExpected files: rostring.c\nAllowed functions: write, malloc, free\n\nWrite a program that rotates the first word of the input string to the end.\nWords are delimited by spaces or tabs.\nOutput words are separated by one space.\nIf argc < 2 print newline.\nIf argc > 2 process only argv[1].',
    'INSANE',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'INSANE'),
    '[{"input":"./rostring \"Que la      lumiere soit et la lumiere fut\"","expected_output":"la lumiere soit et la lumiere fut Que\\n","is_hidden":false},{"input":"./rostring \"     AkjhZ zLKIJz , 23y\"","expected_output":"zLKIJz , 23y AkjhZ\\n","is_hidden":true},{"input":"./rostring \"first\" \"2\" \"11000000\"","expected_output":"first\\n","is_hidden":true},{"input":"./rostring","expected_output":"\\n","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'rostring'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'ft_list_foreach',
    'Assignment name: ft_list_foreach\nExpected files: ft_list_foreach.c, ft_list.h\nAllowed functions: None\n\nWrite a function that iterates through a list and applies function f to each data pointer.\n\nFunction prototype:\nvoid ft_list_foreach(t_list *begin_list, void (*f)(void *));\n\nList structure in ft_list.h:\ntypedef struct s_list\n{\n    struct s_list *next;\n    void          *data;\n} t_list;',
    'INSANE',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'INSANE'),
    '[{"input":"list=[a,b,c], f=collect","expected_output":"f called with a then b then c","is_hidden":false},{"input":"list=NULL, f=collect","expected_output":"no call to f","is_hidden":true},{"input":"list=[x], f=uppercase","expected_output":"x transformed by f once","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'ft_list_foreach'
);
