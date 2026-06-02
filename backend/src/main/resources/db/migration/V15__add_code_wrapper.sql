ALTER TABLE challenges ADD COLUMN IF NOT EXISTS code_wrapper TEXT;

UPDATE challenges
SET code_wrapper = '#include <stdio.h>

int	main(void)
{
	char	str[256];

	if (scanf("%255s", str) == 1)
		printf("%i\n", ft_atoi(str));
	return (0);
}'
WHERE title = 'ft_atoi';
