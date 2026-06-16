-- V21: Add solution_template and test_harness to HARD challenges.
--
-- Goals:
--   1. Users write only their function (no int main).
--   2. test_harness holds the int main that reads stdin and calls the function.
--   3. test_cases use real stdin/expected_output (JSON \n = actual newline).
--
-- Dollar-quoting is used throughout to avoid escaping issues.

-- =====================================================================
-- ft_list_size
-- Stdin format: one integer per line = the node data values (read until EOF).
-- Output: the count of nodes as a decimal integer followed by a newline.
--
-- t_list is defined in the solution_template so both user code and the
-- harness share the same typedef without redefining it.
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <stdlib.h>

typedef struct s_list
{
	struct s_list	*next;
	void			*data;
}	t_list;

/*
** Return the number of elements in the linked list beginning at begin_list.
** Returns 0 if begin_list is NULL.
*/
int	ft_list_size(t_list *begin_list)
{
	return (0);
}
$tpl$,
    test_harness = $harness$
#include <stdlib.h>
#include <stdio.h>

int	main(void)
{
	char	buf[64];
	int		c;
	int		i;
	t_list	*head;
	t_list	*tail;
	t_list	*node;

	head = NULL;
	tail = NULL;
	while (1)
	{
		i = 0;
		while ((c = getchar()) != EOF && c != '\n' && i < 63)
			buf[i++] = (char)c;
		if (i == 0 && c == EOF)
			break ;
		buf[i] = '\0';
		node = malloc(sizeof(t_list));
		node->data = NULL;
		node->next = NULL;
		if (!head)
			head = tail = node;
		else
		{
			tail->next = node;
			tail = node;
		}
		if (c == EOF)
			break ;
	}
	printf("%d\n", ft_list_size(head));
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "",                "expected_output": "0\n",  "is_hidden": false},
        {"input": "42",              "expected_output": "1\n",  "is_hidden": true},
        {"input": "1\n2\n3\n4\n5\n6\n7", "expected_output": "7\n",  "is_hidden": true},
        {"input": "a\nb",            "expected_output": "2\n",  "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'ft_list_size';

-- =====================================================================
-- add_prime_sum
-- Stdin format: one line = the positive integer N.
-- Output: sum of all primes <= N, followed by a newline.
-- If the input is empty, not a positive integer, or <= 0, print "0\n".
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>

/*
** Compute and write the sum of all prime numbers <= n,
** followed by a newline.
** If n < 2, write "0\n".
*/
void	add_prime_sum(int n)
{

}
$tpl$,
    test_harness = $harness$
#include <unistd.h>
#include <stdlib.h>

int	main(void)
{
	char	buf[64];
	int		i;
	char	c;
	long	n;

	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 63)
		buf[i++] = c;
	buf[i] = '\0';
	n = strtol(buf, NULL, 10);
	if (i == 0 || n <= 0)
	{
		write(1, "0\n", 2);
		return (0);
	}
	add_prime_sum((int)n);
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "5",   "expected_output": "10\n",  "is_hidden": false},
        {"input": "7",   "expected_output": "17\n",  "is_hidden": true},
        {"input": "2",   "expected_output": "2\n",   "is_hidden": true},
        {"input": "1",   "expected_output": "0\n",   "is_hidden": true},
        {"input": "20",  "expected_output": "77\n",  "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'add_prime_sum';

-- =====================================================================
-- hidenp
-- Stdin format:
--   Line 1: the needle string (s1)
--   Line 2: the haystack string (s2)
-- Output: "1\n" if s1 is a subsequence of s2, otherwise "0\n".
-- If either line is missing (only one line given), print "0\n".
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>

/*
** Return 1 if every character of s1 appears in s2 in the same order
** (not necessarily contiguous), otherwise return 0.
*/
int	hidenp(char *s1, char *s2)
{
	return (0);
}
$tpl$,
    test_harness = $harness$
#include <unistd.h>
#include <stdio.h>

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
	printf("%d\n", hidenp(s1, s2));
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "abc\n2altrb53c.sse",                    "expected_output": "1\n",  "is_hidden": false},
        {"input": "fgex.;\ntyf34gdgf;ektufjhgdgex.;.;rtjynur6", "expected_output": "1\n",  "is_hidden": true},
        {"input": "abc\nbtarc",                             "expected_output": "0\n",  "is_hidden": true},
        {"input": "\nabc",                                  "expected_output": "1\n",  "is_hidden": true},
        {"input": "abc\nabc",                               "expected_output": "1\n",  "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'hidenp';

-- =====================================================================
-- str_capitalizer
-- Stdin format: one string per line; each line is one "argument".
--   An empty first line means no arguments -> print only a newline.
-- Output: each argument transformed (first letter of each word uppercased,
--   rest lowercased), followed by a newline per argument.
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>

/*
** Capitalise the first letter of every word in str and lowercase the rest.
** A word starts after a space or at the beginning of the string.
** Write the result followed by a newline.
*/
void	str_capitalizer(char *str)
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
	int		got_line;

	got_line = 0;
	while (1)
	{
		i = 0;
		while (read(0, &c, 1) > 0 && c != '\n' && i < 4095)
			buf[i++] = c;
		if (i == 0 && !got_line)
		{
			write(1, "\n", 1);
			return (0);
		}
		if (i == 0)
			break ;
		buf[i] = '\0';
		got_line = 1;
		str_capitalizer(buf);
	}
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "a FiRSt LiTTlE TESt",                             "expected_output": "A First Little Test\n",          "is_hidden": false},
        {"input": "__SecONd teST A LITtle BiT   Moar comPLEX",       "expected_output": "__second Test A Little Bit   Moar Complex\n", "is_hidden": true},
        {"input": "",                                                  "expected_output": "\n",                              "is_hidden": true},
        {"input": "hello world",                                       "expected_output": "Hello World\n",                  "is_hidden": true},
        {"input": "already Capitalized",                               "expected_output": "Already Capitalized\n",          "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'str_capitalizer';

-- =====================================================================
-- tab_mult
-- Stdin format: one line = the strictly positive integer N.
-- Output: the multiplication table 1..9 x N, one result per line:
--   "i x N = result\n"
-- If the input is empty or N <= 0, print only a newline.
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>

/*
** Print the multiplication table from 1 to 9 for n.
** Each line has the format: "i x n = result\n"  (i from 1 to 9).
** If n <= 0, print only a newline.
*/
void	tab_mult(int n)
{

}
$tpl$,
    test_harness = $harness$
#include <unistd.h>
#include <stdlib.h>

int	main(void)
{
	char	buf[64];
	int		i;
	char	c;
	long	n;

	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 63)
		buf[i++] = c;
	buf[i] = '\0';
	n = strtol(buf, NULL, 10);
	if (i == 0 || n <= 0)
	{
		write(1, "\n", 1);
		return (0);
	}
	tab_mult((int)n);
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "9",  "expected_output": "1 x 9 = 9\n2 x 9 = 18\n3 x 9 = 27\n4 x 9 = 36\n5 x 9 = 45\n6 x 9 = 54\n7 x 9 = 63\n8 x 9 = 72\n9 x 9 = 81\n",    "is_hidden": false},
        {"input": "19", "expected_output": "1 x 19 = 19\n2 x 19 = 38\n3 x 19 = 57\n4 x 19 = 76\n5 x 19 = 95\n6 x 19 = 114\n7 x 19 = 133\n8 x 19 = 152\n9 x 19 = 171\n", "is_hidden": true},
        {"input": "",   "expected_output": "\n",  "is_hidden": true},
        {"input": "1",  "expected_output": "1 x 1 = 1\n2 x 1 = 2\n3 x 1 = 3\n4 x 1 = 4\n5 x 1 = 5\n6 x 1 = 6\n7 x 1 = 7\n8 x 1 = 8\n9 x 1 = 9\n", "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'tab_mult';
