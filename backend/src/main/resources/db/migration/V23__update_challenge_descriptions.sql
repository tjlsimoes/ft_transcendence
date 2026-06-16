-- V23: Update challenge descriptions to reflect function-based evaluation.
--
-- Context: V19-V22 added test harnesses to all challenges, converting them from
-- standalone programs to pure C functions. This migration updates descriptions
-- to focus on what users implement (the function logic), removing references to
-- command-line arguments, program execution, and argc/argv checks.
--
-- Philosophy: Code Arena is a competitive duel platform. Descriptions should be
-- concise, action-oriented, and focused on algorithmic correctness and efficiency.

-- =====================================================================
-- EASY CHALLENGES (5)
-- =====================================================================

UPDATE challenges SET
    description = 'Extract the first word from a string, delimited by spaces or tabs.

Function signature:
void first_word(char *str)

Requirements:
- Extract first contiguous sequence of non-space, non-tab characters
- Write result followed by newline
- If string is empty or contains only whitespace, write only newline'
WHERE title = 'first_word';

UPDATE challenges SET
    description = 'Print numbers 1–100 with FizzBuzz substitution logic.

Function signature:
void fizzbuzz(void)

Rules:
- Multiples of 3: "fizz"
- Multiples of 5: "buzz"
- Multiples of both (15): "fizzbuzz"
- All others: the number itself
- One result per line'
WHERE title = 'fizzbuzz';

UPDATE challenges SET
    description = 'Replace all occurrences of a character in a string with another character.

Function signature:
void search_and_replace(char *str, char search, char replace)

Write the result followed by newline.
If search or replace is not exactly one character, write only newline.'
WHERE title = 'search_and_replace';

UPDATE challenges SET
    description = 'Apply ROT13 cipher to a string, preserving case and non-alphabetic characters.

Function signature:
void rot_13(char *str)

- Rotate each letter by 13 positions (a↔n, b↔o, ..., z↔m)
- Preserve uppercase/lowercase distinction
- Keep non-alphabetic characters unchanged
- Write result followed by newline
- If string is empty, write only newline'
WHERE title = 'rot_13';

UPDATE challenges SET
    description = 'Repeat each alphabetic character by its position in the alphabet.

Function signature:
void repeat_alpha(char *str)

- a (1st letter) → appears 1 time
- b (2nd letter) → appears 2 times
- z (26th letter) → appears 26 times
- Preserve case: A repeats 1 time, B repeats 2 times, etc.
- Non-alphabetic characters appear once
- Write result followed by newline
- If string is empty, write only newline'
WHERE title = 'repeat_alpha';

-- =====================================================================
-- MEDIUM CHALLENGES (5)
-- =====================================================================

UPDATE challenges SET
    description = 'Convert a string to an integer, mimicking behavior of standard atoi().

Function signature:
int ft_atoi(const char *str)

Behavior:
- Skip leading whitespace
- Handle optional + or − sign
- Convert consecutive digits until first non-digit
- Return 0 if no valid conversion found'
WHERE title = 'ft_atoi';

UPDATE challenges SET
    description = 'Convert a snake_case string to lowerCamelCase.

Function signature:
void snake_to_camel(char *str)

- Replace underscores with camelCase: word_one_two → wordOneTwo
- First letter remains lowercase
- Letters after underscore are uppercase
- Write result followed by newline'
WHERE title = 'snake_to_camel';

UPDATE challenges SET
    description = 'Print unique characters from two strings without duplicates, preserving order.

Function signature:
void ft_union(char *s1, char *s2)

- Collect all unique characters from s1, then s2
- Preserve first-appearance order
- No duplicates
- Write result followed by newline'
WHERE title = 'union';

UPDATE challenges SET
    description = 'Perform arithmetic operations on two integers.

Function signature:
void do_op(int a, char op, int b)

Supported operators: + − * / %
- Print the integer result followed by newline
- Division by zero: behavior unspecified (no guards required)'
WHERE title = 'do_op';

UPDATE challenges SET
    description = 'Reverse the bits of a byte (8-bit unsigned integer).

Function signature:
unsigned char reverse_bits(unsigned char octet)

Example: 0x26 (0010 0110) → 0x64 (0110 0100)

Return the byte with all bits reversed.'
WHERE title = 'reverse_bits';

-- =====================================================================
-- HARD CHALLENGES (5)
-- =====================================================================

UPDATE challenges SET
    description = 'Count the number of elements in a singly linked list.

Function signature:
int ft_list_size(t_list *begin_list)

List structure:
typedef struct s_list {
    struct s_list *next;
    void *data;
} t_list;

Return 0 if begin_list is NULL.'
WHERE title = 'ft_list_size';

UPDATE challenges SET
    description = 'Calculate the sum of all prime numbers less than or equal to N.

Function signature:
void add_prime_sum(int n)

- Find all primes ≤ N
- Print their sum followed by newline
- If N < 2, print "0\n" (no primes)'
WHERE title = 'add_prime_sum';

UPDATE challenges SET
    description = 'Detect if one string is a hidden subsequence of another.

Function signature:
int hidenp(char *s1, char *s2)

A hidden subsequence means every character of s1 appears in s2 in the same order (not necessarily contiguous).

Return 1 if s1 is hidden in s2, otherwise 0.
Write the result followed by newline.'
WHERE title = 'hidenp';

UPDATE challenges SET
    description = 'Capitalize the first letter of each word and lowercase the rest.

Function signature:
void str_capitalizer(char *str)

- A "word" is delimited by spaces
- First letter of each word: uppercase
- All other letters: lowercase
- Write result followed by newline'
WHERE title = 'str_capitalizer';

UPDATE challenges SET
    description = 'Print a multiplication table for a given integer N (rows 1–9).

Function signature:
void tab_mult(int n)

Format each line as: "i x n = result\n"  where i ranges from 1 to 9.
If N ≤ 0, print only a newline.'
WHERE title = 'tab_mult';

-- =====================================================================
-- INSANE CHALLENGES (5)
-- =====================================================================

UPDATE challenges SET
    description = 'Flood fill a region in a 2D grid from a starting point.

Function signature:
void flood_fill(char **tab, t_point size, t_point begin)

Structure:
typedef struct s_point { int x; int y; } t_point;

Algorithm:
- Start at position (begin.x, begin.y)
- Replace all connected cells (4-connectivity: up/down/left/right only) with the same character as the starting cell with ''F''
- size.x = width, size.y = height
- Edge cases: out-of-bounds begin → no modification; isolated cell → only that cell replaced'
WHERE title = 'flood_fill';

UPDATE challenges SET
    description = 'Split a string into an array of words (dynamic allocation).

Function signature:
char **ft_split(char *str)

Delimiters: spaces, tabs, newlines.

Return:
- NULL-terminated array of allocated word strings
- Empty string (or only whitespace) → empty array (single NULL entry)
- Each word is independently allocated (caller responsibility to free)'
WHERE title = 'ft_split';

UPDATE challenges SET
    description = 'Convert an integer to an allocated null-terminated string.

Function signature:
char *ft_itoa(int nbr)

Requirements:
- Dynamically allocate result
- Handle negative numbers (include − sign)
- Handle INT_MIN edge case (−2147483648)
- Return NULL-terminated string

Caller must free the allocated memory.'
WHERE title = 'ft_itoa';

UPDATE challenges SET
    description = 'Rotate a string: move the first word to the end.

Function signature:
void rostring(char *str)

Algorithm:
- Extract first word (delimited by spaces/tabs)
- Move it to the end, preserving remaining words in order
- Words separated by single space in output
- Write result followed by newline
- If no words or empty string, write only newline'
WHERE title = 'rostring';

UPDATE challenges SET
    description = 'Apply a function to every element of a linked list.

Function signature:
void ft_list_foreach(t_list *begin_list, void (*f)(void *))

List structure:
typedef struct s_list {
    struct s_list *next;
    void *data;
} t_list;

Iterate from begin_list to the end and apply function f to each node''s data pointer.
If begin_list is NULL, do nothing.'
WHERE title = 'ft_list_foreach';
