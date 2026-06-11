-- V20: Add solution_template and test_harness to MEDIUM challenges.
--
-- Goals:
--   1. Users write only their function (no int main).
--   2. test_harness holds the int main that reads stdin and calls the function.
--   3. test_cases are corrected: input is real stdin, expected_output uses
--      JSON \n (actual newline) instead of the old literal \\n.
--
-- Dollar-quoting is used throughout to avoid escaping issues.

-- =====================================================================
-- ft_atoi
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>

/*
** Convert the string str to an integer and return it.
** Behaviour should match the standard atoi(const char *str):
**   - skip leading whitespace
**   - handle optional + or - sign
**   - convert consecutive digits until the first non-digit character
*/
int	ft_atoi(const char *str)
{
	return (0);
}
$tpl$,
    test_harness = $harness$
#include <unistd.h>
#include <stdio.h>

int	main(void)
{
	char	buf[4096];
	int		i;
	char	c;

	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 4095)
		buf[i++] = c;
	buf[i] = '\0';
	printf("%d\n", ft_atoi(buf));
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "42",                "expected_output": "42\n",      "is_hidden": false},
        {"input": "   -214",           "expected_output": "-214\n",    "is_hidden": true},
        {"input": "+00123abc",         "expected_output": "123\n",     "is_hidden": true},
        {"input": "0",                 "expected_output": "0\n",       "is_hidden": true},
        {"input": "   +0",             "expected_output": "0\n",       "is_hidden": true},
        {"input": "-2147483648",       "expected_output": "-2147483648\n", "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'ft_atoi';

-- =====================================================================
-- snake_to_camel
-- Stdin format: one line = the snake_case string to convert.
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>
#include <stdlib.h>

/*
** Convert a snake_case string to lowerCamelCase and write the result
** followed by a newline.
** Example: "here_is_a_snake_case_word" -> "hereIsASnakeCaseWord\n"
*/
void	snake_to_camel(char *str)
{

}
$tpl$,
    test_harness = $harness$
#include <unistd.h>

int	main(void)
{
	char	buf[4096];
	int		i;
	char	c;

	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 4095)
		buf[i++] = c;
	buf[i] = '\0';
	snake_to_camel(buf);
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "here_is_a_snake_case_word",  "expected_output": "hereIsASnakeCaseWord\n",  "is_hidden": false},
        {"input": "hello_world",                "expected_output": "helloWorld\n",             "is_hidden": true},
        {"input": "already",                    "expected_output": "already\n",                "is_hidden": true},
        {"input": "a_b_c",                      "expected_output": "aBC\n",                   "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'snake_to_camel';

-- =====================================================================
-- union
-- Stdin format: line 1 = first string, line 2 = second string.
-- Prints, without duplicates, the characters that appear in either
-- string, preserving their first-appearance order, followed by a newline.
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>

/*
** Take two strings and write, without duplicates, the characters
** that appear in either string, in the order they first appear,
** followed by a newline.
*/
void	ft_union(char *s1, char *s2)
{

}
$tpl$,
    test_harness = $harness$
#include <unistd.h>

int	main(void)
{
	char	s1[4096];
	char	s2[4096];
	int		i;
	char	c;

	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 4095)
		s1[i++] = c;
	s1[i] = '\0';
	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 4095)
		s2[i++] = c;
	s2[i] = '\0';
	ft_union(s1, s2);
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "zpadinton\npaqefwtdjetyiytjneytjoeyjnejeyj",   "expected_output": "zpadintoqefwjy\n",  "is_hidden": false},
        {"input": "ddf6vewg64f\ngtwthgdwthdwfteewhrtag6h4ffdhsd", "expected_output": "df6vewg4thras\n",   "is_hidden": true},
        {"input": "aaaa\nbbbb",                                   "expected_output": "ab\n",              "is_hidden": true},
        {"input": "abc\nabc",                                      "expected_output": "abc\n",             "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'union';

-- =====================================================================
-- do_op
-- Stdin format: line 1 = left operand, line 2 = operator, line 3 = right operand.
-- Supported operators: + - * / %
-- Prints the result followed by a newline.
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

/*
** Perform the arithmetic operation:  left_operand  operator  right_operand.
** Supported operators: + - * / %
** Print the integer result followed by a newline.
*/
void	do_op(int a, char op, int b)
{

}
$tpl$,
    test_harness = $harness$
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

int	main(void)
{
	char	left[64];
	char	oper[64];
	char	right[64];
	int		i;
	char	c;

	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 63)
		left[i++] = c;
	left[i] = '\0';
	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 63)
		oper[i++] = c;
	oper[i] = '\0';
	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 63)
		right[i++] = c;
	right[i] = '\0';
	do_op(atoi(left), oper[0], atoi(right));
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "123\n*\n456",    "expected_output": "56088\n",   "is_hidden": false},
        {"input": "9828\n/\n234",   "expected_output": "42\n",      "is_hidden": true},
        {"input": "1\n+\n-43",     "expected_output": "-42\n",     "is_hidden": true},
        {"input": "42\n%\n10",     "expected_output": "2\n",       "is_hidden": true},
        {"input": "100\n-\n58",    "expected_output": "42\n",      "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'do_op';

-- =====================================================================
-- reverse_bits
-- Stdin format: one line = the byte value in decimal (0-255).
-- Prints the reversed byte value in hex prefixed with 0x.
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$

/*
** Take one byte and return it with its bits reversed.
** Example: 0x26 (0010 0110) -> 0x64 (0110 0100)
*/
unsigned char	reverse_bits(unsigned char octet)
{
	return (0);
}
$tpl$,
    test_harness = $harness$
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

int	main(void)
{
	char	buf[64];
	int		i;
	char	c;

	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 63)
		buf[i++] = c;
	buf[i] = '\0';
	printf("0x%02x\n", reverse_bits((unsigned char)strtol(buf, NULL, 0)));
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "0x26",   "expected_output": "0x64\n",  "is_hidden": false},
        {"input": "0x01",   "expected_output": "0x80\n",  "is_hidden": true},
        {"input": "0x00",   "expected_output": "0x00\n",  "is_hidden": true},
        {"input": "0xFF",   "expected_output": "0xff\n",  "is_hidden": true},
        {"input": "0xA5",   "expected_output": "0xa5\n",  "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'reverse_bits';
