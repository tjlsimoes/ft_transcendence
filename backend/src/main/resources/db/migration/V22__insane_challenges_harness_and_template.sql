-- V22: Add solution_template and test_harness to INSANE challenges.
--
-- Goals:
--   1. Users write only their function (no int main).
--   2. test_harness holds the int main that reads stdin and calls the function.
--   3. test_cases use real stdin/expected_output (JSON \n = actual newline).
--
-- Dollar-quoting is used throughout to avoid escaping issues.

-- =====================================================================
-- flood_fill
-- Stdin format:
--   Line 1  : width height
--   Lines 2..(height+1): each row of the grid (compact string, no spaces)
--   Last line: begin_x begin_y
-- Output: the modified grid, one row per line.
--
-- t_point is defined in the solution_template (before the function),
-- so the harness can use it without redefining it.
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <stdlib.h>

typedef struct s_point
{
	int	x;
	int	y;
}	t_point;

/*
** Fill the connected region at begin with 'F'.
** Only horizontal and vertical neighbours sharing the same character
** as tab[begin.y][begin.x] are included in the region.
** size.x = number of columns, size.y = number of rows.
*/
void	flood_fill(char **tab, t_point size, t_point begin)
{

}
$tpl$,
    test_harness = $harness$
#include <stdlib.h>
#include <stdio.h>

int	main(void)
{
	int		width;
	int		height;
	int		bx;
	int		by;
	char	**tab;
	t_point	size;
	t_point	begin;
	int		i;

	scanf("%d %d", &width, &height);
	tab = malloc((height + 1) * sizeof(char *));
	i = 0;
	while (i < height)
	{
		tab[i] = malloc((width + 1) * sizeof(char));
		scanf("%s", tab[i]);
		i++;
	}
	tab[height] = NULL;
	scanf("%d %d", &bx, &by);
	size.x = width;
	size.y = height;
	begin.x = bx;
	begin.y = by;
	flood_fill(tab, size, begin);
	i = 0;
	while (i < height)
	{
		printf("%s\n", tab[i]);
		i++;
	}
	i = 0;
	while (i < height)
	{
		free(tab[i]);
		i++;
	}
	free(tab);
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "8 5\n11111111\n10001001\n10010001\n10110001\n11100001\n7 4",   "expected_output": "FFFFFFFF\nF000F00F\nF00F000F\nF0FF000F\nFFF0000F\n", "is_hidden": false},
        {"input": "3 3\n010\n000\n010\n1 0",                                      "expected_output": "0F0\n000\n010\n",                                    "is_hidden": true},
        {"input": "3 2\n111\n111\n0 0",                                            "expected_output": "FFF\nFFF\n",                                          "is_hidden": true},
        {"input": "5 3\n11111\n10001\n11111\n2 1",                                 "expected_output": "11111\n1FFF1\n11111\n",                               "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'flood_fill';

-- =====================================================================
-- ft_split
-- Stdin format: the full string to split (read until EOF).
-- Delimiters: spaces, tabs, newlines.
-- Output: each word on its own line.
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <stdlib.h>

/*
** Split str into words delimited by spaces, tabs, or newlines.
** Returns a NULL-terminated array of allocated strings.
** Returns NULL on allocation failure.
*/
char	**ft_split(char *str)
{
	return (NULL);
}
$tpl$,
    test_harness = $harness$
#include <stdlib.h>
#include <stdio.h>

int	main(void)
{
	char	buf[4096];
	int		i;
	int		c;
	char	**words;
	int		j;

	i = 0;
	while ((c = getchar()) != EOF && i < 4095)
		buf[i++] = (char)c;
	buf[i] = '\0';
	words = ft_split(buf);
	if (words)
	{
		j = 0;
		while (words[j])
		{
			printf("%s\n", words[j]);
			free(words[j]);
			j++;
		}
		free(words);
	}
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "   Hello\t42\nLisbon   ",   "expected_output": "Hello\n42\nLisbon\n",    "is_hidden": false},
        {"input": "single",                    "expected_output": "single\n",               "is_hidden": true},
        {"input": "a  b\tc\nd",               "expected_output": "a\nb\nc\nd\n",           "is_hidden": true},
        {"input": "  leading trailing  ",      "expected_output": "leading\ntrailing\n",    "is_hidden": true},
        {"input": "one two three",             "expected_output": "one\ntwo\nthree\n",      "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'ft_split';

-- =====================================================================
-- ft_itoa
-- Stdin format: one line containing the integer to convert.
-- Output: the integer as a string followed by a newline.
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <stdlib.h>

/*
** Convert the integer nbr to a null-terminated allocated string.
** Returns NULL on allocation failure.
*/
char	*ft_itoa(int nbr)
{
	return (NULL);
}
$tpl$,
    test_harness = $harness$
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>

int	main(void)
{
	char	buf[64];
	int		i;
	char	c;
	char	*result;

	i = 0;
	while (read(0, &c, 1) > 0 && c != '\n' && i < 63)
		buf[i++] = c;
	buf[i] = '\0';
	result = ft_itoa((int)strtol(buf, NULL, 10));
	if (result)
	{
		printf("%s\n", result);
		free(result);
	}
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "0",           "expected_output": "0\n",            "is_hidden": false},
        {"input": "-42",         "expected_output": "-42\n",          "is_hidden": true},
        {"input": "-2147483648", "expected_output": "-2147483648\n",  "is_hidden": true},
        {"input": "2147483647",  "expected_output": "2147483647\n",   "is_hidden": true},
        {"input": "42",          "expected_output": "42\n",           "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'ft_itoa';

-- =====================================================================
-- rostring
-- Stdin format: one line containing the string to process.
-- Output: first word moved to end, words separated by one space, then newline.
-- If the string has no words, write only a newline.
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <unistd.h>
#include <stdlib.h>

/*
** Rotate the first word of str to the end.
** Words are separated by spaces or tabs.
** Output words are separated by a single space, followed by a newline.
** If str contains no words, write only a newline.
*/
void	rostring(char *str)
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
	rostring(buf);
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "Que la      lumiere soit et la lumiere fut",   "expected_output": "la lumiere soit et la lumiere fut Que\n",  "is_hidden": false},
        {"input": "     AkjhZ zLKIJz , 23y",                     "expected_output": "zLKIJz , 23y AkjhZ\n",                    "is_hidden": true},
        {"input": "",                                             "expected_output": "\n",                                      "is_hidden": true},
        {"input": "single",                                       "expected_output": "single\n",                                "is_hidden": true},
        {"input": "a b",                                          "expected_output": "b a\n",                                   "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'rostring';

-- =====================================================================
-- ft_list_foreach
-- Stdin format: one node data per line (strings).
--   EOF terminates the list.
-- Output: each node's data printed by the applied function, one per line.
--
-- t_list is defined in the solution_template (before the function),
-- so the harness can use it without redefining it.
-- =====================================================================
UPDATE challenges SET
    solution_template = $tpl$
#include <stdlib.h>
#include <string.h>

typedef struct s_list
{
	struct s_list	*next;
	void			*data;
}	t_list;

/*
** Apply function f to the data pointer of each node in begin_list.
** Iterates from begin_list to the end of the list.
*/
void	ft_list_foreach(t_list *begin_list, void (*f)(void *))
{

}
$tpl$,
    test_harness = $harness$
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

static void	print_str(void *data)
{
	printf("%s\n", (char *)data);
}

int	main(void)
{
	char	buf[256];
	int		i;
	int		c;
	t_list	*head;
	t_list	*tail;
	t_list	*node;

	head = NULL;
	tail = NULL;
	while (1)
	{
		i = 0;
		while ((c = getchar()) != EOF && c != '\n' && i < 255)
			buf[i++] = (char)c;
		if (i == 0 && c == EOF)
			break ;
		buf[i] = '\0';
		node = malloc(sizeof(t_list));
		node->data = strdup(buf);
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
	ft_list_foreach(head, print_str);
	return (0);
}
$harness$,
    test_cases = $tc$[
        {"input": "a\nb\nc",         "expected_output": "a\nb\nc\n",           "is_hidden": false},
        {"input": "hello",           "expected_output": "hello\n",             "is_hidden": true},
        {"input": "42\nworld\nfoo",  "expected_output": "42\nworld\nfoo\n",    "is_hidden": true},
        {"input": "x\ny\nz\nw",      "expected_output": "x\ny\nz\nw\n",        "is_hidden": true}
    ]$tc$::jsonb
WHERE title = 'ft_list_foreach';
