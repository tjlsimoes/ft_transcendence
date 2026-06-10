-- V19: Add solution_template and test_harness to EASY challenges.
--
-- Goals:
--   1. Users write only their function (no int main).
--   2. test_harness holds the int main that reads stdin and calls the function.
--   3. test_cases are corrected: input is real stdin, expected_output uses
--      JSON \n (actual newline) instead of the old literal \\n.
--
-- Dollar-quoting is used throughout to avoid escaping issues.

-- =====================================================================
-- first_word
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>

/*
** Write the first word of str followed by a newline.
** Words are delimited by spaces or tabs.
** If str is empty or contains no words, write only a newline.
*/
void	first_word(char *str)
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
	first_word(buf);
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "FOR PONY",                                                     "expected_output": "FOR\n",          "is_hidden": false},
        {"input": "this        ...    is sparta, then again, maybe    not",        "expected_output": "this\n",         "is_hidden": true},
        {"input": "   ",                                                           "expected_output": "\n",             "is_hidden": true},
        {"input": "",                                                              "expected_output": "\n",             "is_hidden": true},
        {"input": "  lorem,ipsum  ",                                               "expected_output": "lorem,ipsum\n",  "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'first_word';

-- =====================================================================
-- fizzbuzz
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>

/*
** Print numbers from 1 to 100, one per line.
** Multiples of 3   -> "fizz"
** Multiples of 5   -> "buzz"
** Multiples of 3&5 -> "fizzbuzz"
*/
void	fizzbuzz(void)
{

}
$tpl$,
    test_harness = $harness$
int	main(void)
{
	fizzbuzz();
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "", "expected_output": "1\n2\nfizz\n4\nbuzz\nfizz\n7\n8\nfizz\nbuzz\n11\nfizz\n13\n14\nfizzbuzz\n16\n17\nfizz\n19\nbuzz\nfizz\n22\n23\nfizz\nbuzz\n26\nfizz\n28\n29\nfizzbuzz\n31\n32\nfizz\n34\nbuzz\nfizz\n37\n38\nfizz\nbuzz\n41\nfizz\n43\n44\nfizzbuzz\n46\n47\nfizz\n49\nbuzz\nfizz\n52\n53\nfizz\nbuzz\n56\nfizz\n58\n59\nfizzbuzz\n61\n62\nfizz\n64\nbuzz\nfizz\n67\n68\nfizz\nbuzz\n71\nfizz\n73\n74\nfizzbuzz\n76\n77\nfizz\n79\nbuzz\nfizz\n82\n83\nfizz\nbuzz\n86\nfizz\n88\n89\nfizzbuzz\n91\n92\nfizz\n94\nbuzz\nfizz\n97\n98\nfizz\nbuzz\n", "is_hidden": false}
    ]$tc$::jsonb
WHERE title = 'fizzbuzz';

-- =====================================================================
-- search_and_replace
-- Stdin format: line 1 = string, line 2 = search char, line 3 = replace char.
-- If search or replace is not exactly 1 char, print only a newline.
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>

/*
** Replace every occurrence of 'search' in str with 'replace',
** then write the result followed by a newline.
*/
void	search_and_replace(char *str, char search, char replace)
{

}
$tpl$,
    test_harness = $harness$
#include <unistd.h>
#include <string.h>

int	main(void)
{
	char	str[4096];
	char	search[64];
	char	rep[64];
	int		i;
	char	c;

	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 4095)
		str[i++] = c;
	str[i] = '\0';
	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 63)
		search[i++] = c;
	search[i] = '\0';
	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 63)
		rep[i++] = c;
	rep[i] = '\0';
	if (strlen(search) != 1 || strlen(rep) != 1)
	{
		write(1, "\n", 1);
		return (0);
	}
	search_and_replace(str, search[0], rep[0]);
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "Papache est un sabre\na\no",              "expected_output": "Popoche est un sobre\n",      "is_hidden": false},
        {"input": "zaz\nart\nzul",                           "expected_output": "\n",                          "is_hidden": true},
        {"input": "zaz\nr\nu",                               "expected_output": "zaz\n",                       "is_hidden": true},
        {"input": "ZoZ eT Dovid oiME le METol.\no\na",       "expected_output": "ZaZ eT David aiME le METal.\n", "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'search_and_replace';

-- =====================================================================
-- rot_13
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>

/*
** Apply ROT13 to every letter of str, preserve case and non-letters,
** then write the result followed by a newline.
** If str is empty, write only a newline.
*/
void	rot_13(char *str)
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
	rot_13(buf);
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "abc",                  "expected_output": "nop\n",                  "is_hidden": false},
        {"input": "My horse is Amazing.", "expected_output": "Zl ubefr vf Nznmvat.\n", "is_hidden": true},
        {"input": "AkjhZ zLKIJz , 23y ", "expected_output": "NxwuM mYXVWm , 23l \n", "is_hidden": true},
        {"input": "",                     "expected_output": "\n",                      "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'rot_13';

-- =====================================================================
-- repeat_alpha
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>

/*
** Repeat each alphabetical character in str by its alphabet index
** (a=1, b=2, ..., z=26), preserving case. Non-alpha chars are kept as-is.
** Write the result followed by a newline.
** If str is empty, write only a newline.
*/
void	repeat_alpha(char *str)
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
	repeat_alpha(buf);
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "abc",           "expected_output": "abbccc\n",                                        "is_hidden": false},
        {"input": "Alex.",         "expected_output": "Alllllllllllleeeeexxxxxxxxxxxxxxxxxxxxxxxx.\n",    "is_hidden": true},
        {"input": "abacadaba 42!", "expected_output": "abbacccaddddabba 42!\n",                          "is_hidden": true},
        {"input": "",              "expected_output": "\n",                                              "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'repeat_alpha';
