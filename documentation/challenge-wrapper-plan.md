# Plano: Converter Challenges para Função + Stdin

## Estratégia

O usuário escreve **apenas a função**. O `code_wrapper` salvo no banco fornece o `main` que:
1. Lê os argumentos do **stdin**
2. Chama a função do usuário
3. Imprime o resultado

Os test cases têm o campo `input` atualizado para o valor real do stdin (não mais `./programa "arg"`).

---

## Status

| # | Challenge | Dificuldade | Tipo | Status |
|---|-----------|-------------|------|--------|
| 1 | `ft_atoi` | MEDIUM | Função | ✅ Feito |
| 2 | `first_word` | EASY | Programa | ⬜ Pendente |
| 3 | `fizzbuzz` | EASY | Programa | ⬜ Pendente |
| 4 | `search_and_replace` | EASY | Programa | ⬜ Pendente |
| 5 | `rot_13` | EASY | Programa | ⬜ Pendente |
| 6 | `repeat_alpha` | EASY | Programa | ⬜ Pendente |
| 7 | `snake_to_camel` | MEDIUM | Programa | ⬜ Pendente |
| 8 | `union` | MEDIUM | Programa | ⬜ Pendente |
| 9 | `do_op` | MEDIUM | Programa | ⬜ Pendente |
| 10 | `reverse_bits` | MEDIUM | Função | ⬜ Pendente |
| 11 | `add_prime_sum` | HARD | Programa | ⬜ Pendente |
| 12 | `hidenp` | HARD | Programa | ⬜ Pendente |
| 13 | `str_capitalizer` | HARD | Programa | ⬜ Pendente |
| 14 | `tab_mult` | HARD | Programa | ⬜ Pendente |
| 15 | `ft_list_size` | HARD | Função | ⬜ Pendente |
| 16 | `ft_itoa` | INSANE | Função | ⬜ Pendente |
| 17 | `ft_split` | INSANE | Função | ⬜ Pendente |
| 18 | `rostring` | INSANE | Programa | ⬜ Pendente |
| 19 | `ft_list_foreach` | INSANE | Função | ⬜ Pendente |
| 20 | `flood_fill` | INSANE | Função | ⬜ Pendente |

---

## Passo a Passo por Challenge

Para cada challenge é preciso fazer:
1. Atualizar os `test_cases` (campo `input`) no arquivo de seed SQL
2. Escrever o `code_wrapper` (main que lê stdin e chama a função)
3. Criar uma migration `V1x__fix_<title>.sql` que aplica o UPDATE no banco

---

## 1. ft_atoi ✅ FEITO

**Protótipo:** `int ft_atoi(const char *str);`

**Stdin:** a string a converter (ex: `42`)

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    char str[256];
    if (scanf("%255s", str) == 1)
        printf("%i\n", ft_atoi(str));
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `42` | `42` |
| `   -214` | `-214` |
| `+00123abc` | `123` |

---

## 2. first_word

**Protótipo:** `void ft_first_word(char *str);` — imprime a primeira palavra + `\n`

**Stdin:** a string inteira (1 linha)

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    char str[4096];
    if (fgets(str, sizeof(str), stdin))
        ft_first_word(str);
    else
        write(1, "\n", 1);
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `FOR PONY` | `FOR\n` |
| `this        ...    is sparta, then again, maybe    not` | `this\n` |
| `   ` | `\n` |
| *(vazio)* | `\n` |
| `  lorem,ipsum  ` | `lorem,ipsum\n` |

> **Nota:** O caso `"a" "b"` (argc != 2) vira stdin vazio → imprime `\n`

---

## 3. fizzbuzz

**Protótipo:** `void ft_fizzbuzz(void);` — imprime de 1 a 100

**Stdin:** nenhum (a função não recebe argumento)

**Wrapper:**
```c
int main(void)
{
    ft_fizzbuzz();
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| *(vazio)* | `1\n2\nfizz\n4\nbuzz\nfizz\n7\n8\nfizz\nbuzz\n11\nfizz\n13\n14\nfizzbuzz\n16\n17\nfizz\n19\nbuzz\nfizz\n22\n23\nfizz\nbuzz\n26\nfizz\n28\n29\nfizzbuzz\n31\n32\nfizz\n34\nbuzz\nfizz\n37\n38\nfizz\nbuzz\n41\nfizz\n43\n44\nfizzbuzz\n46\n47\nfizz\n49\nbuzz\nfizz\n52\n53\nfizz\nbuzz\n56\nfizz\n58\n59\nfizzbuzz\n61\n62\nfizz\n64\nbuzz\nfizz\n67\n68\nfizz\nbuzz\n71\nfizz\n73\n74\nfizzbuzz\n76\n77\nfizz\n79\nbuzz\nfizz\n82\n83\nfizz\nbuzz\n86\nfizz\n88\n89\nfizzbuzz\n91\n92\nfizz\n94\nbuzz\nfizz\n97\n98\nfizz\nbuzz\n` |

> Os test cases anteriores (`line 15 → fizzbuzz`) não fazem sentido como stdin. Substituir pelos valores reais de saída verificando linha por linha não é viável; usar apenas o caso completo como único test case visível, e casos parciais como hidden são removidos ou adaptados.

---

## 4. search_and_replace

**Protótipo:** `void ft_search_and_replace(char *str, char search, char replace);`

**Stdin:** 3 linhas — `str`, `search` (1 char), `replace` (1 char)

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    char str[4096];
    char search[4];
    char replace[4];

    if (fgets(str, sizeof(str), stdin) == NULL
        || scanf(" %1s", search) != 1
        || scanf(" %1s", replace) != 1)
    {
        write(1, "\n", 1);
        return (0);
    }
    ft_search_and_replace(str, search[0], replace[0]);
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `Papache est un sabre\na\no` | `Popoche est un sobre\n` |
| `zaz\nart\nzul` | `\n` (search tem >1 char → inválido) |
| `zaz\nr\nu` | `zaz\n` |
| `ZoZ eT Dovid oiME le METol.\no\na` | `ZaZ eT David aiME le METal.\n` |

> **Nota:** O wrapper valida que search/replace são exatamente 1 char.

---

## 5. rot_13

**Protótipo:** `void ft_rot13(char *str);` — imprime str com ROT13 + `\n`

**Stdin:** a string (1 linha)

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    char str[4096];
    if (fgets(str, sizeof(str), stdin))
        ft_rot13(str);
    else
        write(1, "\n", 1);
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `abc` | `nop\n` |
| `My horse is Amazing.` | `Zl ubefr vf Nznmvat.\n` |
| `AkjhZ zLKIJz , 23y ` | `NxwuM mYXVWm , 23l \n` |
| *(vazio)* | `\n` |

---

## 6. repeat_alpha

**Protótipo:** `void ft_repeat_alpha(char *str);`

**Stdin:** a string (1 linha)

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    char str[4096];
    if (fgets(str, sizeof(str), stdin))
        ft_repeat_alpha(str);
    else
        write(1, "\n", 1);
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `abc` | `abbccc\n` |
| `Alex.` | `Allllllllllleeeeexxxxxxxxxxxxxxxxxxxxxxxx.\n` |
| `abacadaba 42!` | `abbacccaddddabba 42!\n` |
| *(vazio)* | `\n` |

---

## 7. snake_to_camel

**Protótipo:** `void ft_snake_to_camel(char *str);`

**Stdin:** a string snake_case (1 linha)

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    char str[4096];
    if (fgets(str, sizeof(str), stdin))
        ft_snake_to_camel(str);
    else
        write(1, "\n", 1);
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `here_is_a_snake_case_word` | `hereIsASnakeCaseWord\n` |
| `hello_world` | `helloWorld\n` |
| *(vazio)* | `\n` |

---

## 8. union

**Protótipo:** `void ft_union(char *s1, char *s2);`

**Stdin:** 2 linhas — s1, s2

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    char s1[4096];
    char s2[4096];

    if (fgets(s1, sizeof(s1), stdin) == NULL
        || fgets(s2, sizeof(s2), stdin) == NULL)
    {
        write(1, "\n", 1);
        return (0);
    }
    ft_union(s1, s2);
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `zpadinton\npaqefwtdjetyiytjneytjoeyjnejeyj` | `zpadintoqefwjy\n` |
| `ddf6vewg64f\ngtwthgdwthdwfteewhrtag6h4ffdhsd` | `df6vewg4thras\n` |
| *(vazio)* | `\n` |

---

## 9. do_op

**Protótipo:** `void ft_do_op(int a, char op, int b);`

**Stdin:** 3 linhas — número, operador, número

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    int a;
    char op[4];
    int b;

    if (scanf("%d %1s %d", &a, op, &b) != 3)
    {
        write(1, "\n", 1);
        return (0);
    }
    ft_do_op(a, op[0], b);
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `123 * 456` | `56088\n` |
| `9828 / 234` | `42\n` |
| `1 + -43` | `-42\n` |
| *(vazio)* | `\n` |

---

## 10. reverse_bits

**Protótipo:** `unsigned char ft_reverse_bits(unsigned char octet);`

**Stdin:** valor decimal do byte (0–255)

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    unsigned int n;
    if (scanf("%u", &n) == 1)
        printf("0x%02X\n", ft_reverse_bits((unsigned char)n));
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `38` | `0x64` |
| `1` | `0x80` |
| `0` | `0x00` |

> `0x26` = 38 decimal, `0x01` = 1, `0x00` = 0

---

## 11. add_prime_sum

**Protótipo:** `int ft_add_prime_sum(int n);`

**Stdin:** número inteiro positivo

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    int n;
    if (scanf("%d", &n) == 1 && n > 0)
        printf("%d\n", ft_add_prime_sum(n));
    else
        printf("0\n");
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `5` | `10\n` |
| `7` | `17\n` |
| *(vazio)* | `0\n` |
| `-3` | `0\n` |

---

## 12. hidenp

**Protótipo:** `int ft_hidenp(char *s1, char *s2);` — retorna 1 se s1 é subsequência de s2

**Stdin:** 2 linhas — s1, s2

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    char s1[4096];
    char s2[4096];

    if (fgets(s1, sizeof(s1), stdin) == NULL
        || fgets(s2, sizeof(s2), stdin) == NULL)
    {
        write(1, "\n", 1);
        return (0);
    }
    printf("%d\n", ft_hidenp(s1, s2));
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `abc\n2altrb53c.sse` | `1\n` |
| `fgex.;\ntyf34gdgf;ektufjhgdgex.;.;rtjynur6` | `1\n` |
| `abc\nbtarc` | `0\n` |
| *(vazio)* | `\n` |

---

## 13. str_capitalizer

**Protótipo:** `void ft_str_capitalizer(char *str);`

**Stdin:** a string (1 linha)

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    char str[4096];
    if (fgets(str, sizeof(str), stdin))
        ft_str_capitalizer(str);
    else
        write(1, "\n", 1);
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `a FiRSt LiTTlE TESt` | `A First Little Test\n` |
| `__SecONd teST A LITtle BiT   Moar comPLEX` | `__second Test A Little Bit   Moar Complex\n` |
| *(vazio)* | `\n` |

---

## 14. tab_mult

**Protótipo:** `void ft_tab_mult(int n);`

**Stdin:** número inteiro positivo

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    int n;
    if (scanf("%d", &n) == 1 && n > 0)
        ft_tab_mult(n);
    else
        write(1, "\n", 1);
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `9` | `1 x 9 = 9\n2 x 9 = 18\n3 x 9 = 27\n4 x 9 = 36\n5 x 9 = 45\n6 x 9 = 54\n7 x 9 = 63\n8 x 9 = 72\n9 x 9 = 81\n` |
| `19` | `1 x 19 = 19\n2 x 19 = 38\n3 x 19 = 57\n4 x 19 = 76\n5 x 19 = 95\n6 x 19 = 114\n7 x 19 = 133\n8 x 19 = 152\n9 x 19 = 171\n` |
| *(vazio)* | `\n` |

---

## 15. ft_list_size

**Protótipo:** `int ft_list_size(t_list *begin_list);`

**Stdin:** número de nós da lista (0 = NULL)

**Wrapper:**
```c
#include <stdio.h>
#include <stdlib.h>

typedef struct s_list
{
    struct s_list *next;
    void          *data;
} t_list;

int main(void)
{
    int n;
    t_list *head = NULL;
    t_list *node;

    if (scanf("%d", &n) != 1 || n < 0)
        n = 0;
    for (int i = 0; i < n; i++)
    {
        node = malloc(sizeof(t_list));
        node->data = NULL;
        node->next = head;
        head = node;
    }
    printf("%d\n", ft_list_size(head));
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `0` | `0` |
| `1` | `1` |
| `7` | `7` |

---

## 16. ft_itoa

**Protótipo:** `char *ft_itoa(int nbr);`

**Stdin:** número inteiro

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    int n;
    char *result;

    if (scanf("%d", &n) == 1)
    {
        result = ft_itoa(n);
        if (result)
        {
            printf("%s\n", result);
            free(result);
        }
    }
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `0` | `0` |
| `-42` | `-42` |
| `-2147483648` | `-2147483648` |
| `2147483647` | `2147483647` |

---

## 17. ft_split

**Protótipo:** `char **ft_split(char *str);`

**Stdin:** a string a dividir (1 linha)

**Wrapper:**
```c
#include <stdio.h>
#include <stdlib.h>

int main(void)
{
    char str[4096];
    char **result;
    int i;

    if (fgets(str, sizeof(str), stdin) == NULL)
        return (0);
    result = ft_split(str);
    if (!result)
        return (0);
    printf("[");
    for (i = 0; result[i]; i++)
    {
        if (i > 0)
            printf(",");
        printf("%s", result[i]);
        free(result[i]);
    }
    printf("]\n");
    free(result);
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `   Hello\t42\nLisbon   ` | `[Hello,42,Lisbon]` |
| `\n\t   ` | `[]` |
| `single` | `[single]` |

---

## 18. rostring

**Protótipo:** `void ft_rostring(char *str);` — imprime a string com a primeira palavra movida para o fim

**Stdin:** a string (1 linha)

**Wrapper:**
```c
#include <stdio.h>

int main(void)
{
    char str[4096];
    if (fgets(str, sizeof(str), stdin))
        ft_rostring(str);
    else
        write(1, "\n", 1);
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `Que la      lumiere soit et la lumiere fut` | `la lumiere soit et la lumiere fut Que\n` |
| `     AkjhZ zLKIJz , 23y` | `zLKIJz , 23y AkjhZ\n` |
| *(vazio)* | `\n` |

> O caso de `argc > 2` (apenas argv[1] é processado) vira um único argumento no stdin — comportamento equivalente.

---

## 19. ft_list_foreach

**Protótipo:** `void ft_list_foreach(t_list *begin_list, void (*f)(void *));`

**Stdin:** número de nós seguido dos dados (strings) de cada nó

**Wrapper:**
```c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct s_list
{
    struct s_list *next;
    void          *data;
} t_list;

static void print_data(void *data)
{
    printf("%s\n", (char *)data);
}

int main(void)
{
    int n;
    char buf[256];
    t_list *head = NULL;
    t_list **cur = &head;
    t_list *node;

    if (scanf("%d\n", &n) != 1 || n < 0)
        n = 0;
    for (int i = 0; i < n; i++)
    {
        if (!fgets(buf, sizeof(buf), stdin))
            break;
        buf[strcspn(buf, "\n")] = '\0';
        node = malloc(sizeof(t_list));
        node->data = strdup(buf);
        node->next = NULL;
        *cur = node;
        cur = &node->next;
    }
    ft_list_foreach(head, print_data);
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `3\na\nb\nc` | `a\nb\nc\n` |
| `0` | *(vazio)* |
| `1\nx` | `x\n` |

---

## 20. flood_fill

**Protótipo:** `void flood_fill(char **tab, t_point size, t_point begin);`

**Stdin:** `cols rows begin_x begin_y` na 1ª linha, depois `rows` linhas com strings de `cols` chars (`0` ou `1`)

**Wrapper:**
```c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct s_point { int x; int y; } t_point;

int main(void)
{
    int cols, rows, bx, by;
    if (scanf("%d %d %d %d\n", &cols, &rows, &bx, &by) != 4)
        return (1);

    char **tab = malloc((rows + 1) * sizeof(char *));
    char buf[1024];
    for (int i = 0; i < rows; i++)
    {
        if (!fgets(buf, sizeof(buf), stdin))
            return (1);
        buf[strcspn(buf, "\n")] = '\0';
        tab[i] = strdup(buf);
    }
    tab[rows] = NULL;

    t_point size  = {cols, rows};
    t_point begin = {bx, by};
    flood_fill(tab, size, begin);

    printf("[");
    for (int i = 0; i < rows; i++)
    {
        if (i > 0) printf(";");
        printf("%s", tab[i]);
        free(tab[i]);
    }
    printf("]\n");
    free(tab);
    return (0);
}
```

**Test cases:**
| input | expected_output |
|-------|----------------|
| `8 5 7 4\n11111111\n10001001\n10010001\n10110001\n11100001` | `[FFFFFFFF;F000F00F;F00F000F;F0FF000F;FFF0000F]` |
| `3 3 1 1\n111\n101\n111` | `[111;1F1;111]` (célula isolada) |
| `3 3 5 5\n111\n111\n111` | `[111;111;111]` (out of bounds, sem alteração) |

---

## Processo para cada challenge

```
1. Atualizar input dos test_cases no arquivo de seed (.sql)
2. Escrever o code_wrapper acima
3. Criar migration V1x__fix_<title>.sql com:
   UPDATE challenges
   SET test_cases = '...',
       code_wrapper = '...'
   WHERE title = '<title>';
4. docker compose up -d --build backend
5. Testar na arena
6. Marcar como ✅ neste documento
```
