# Лабораторная работа: Семейное дерево на Prolog

## Установка SWI-Prolog

Для Ubuntu/Debian:
```bash
sudo apt install swi-prolog
```

## Запуск

```bash
# Тест всех 17 запросов
swipl -l family.pl -l queries.pl -g "run_queries" -t halt

# Тест динамических операций (birth, marriage, divorce, death)
swipl -l family.pl -l dynamic_demo.pl -g "run_demo" -t halt

# Интерактивный режим
swipl -l family.pl
```

## Факты и собрание семьи (30 человек)

Семья разделена на 4 поколения (g1–g4):

| Поколение | Члены | Описание |
|-----------|-------|----------|
| g1 (4) | fedor, vasilisa, ivan, ekaterina | прадедушки/прабабушки |
| g2 (6) | sergey, nina, alexey, tamara, boris, lydia | дедушки/бабушки |
| g3 (8) | michael, oksana, andrey, irina, dmitry, olga, viktor, elena | родители |
| g4 (12) | anna, pavel, maria, nikita, sofia, artem, kate, leonid, diana, maxim, victoria, danil | дети |

Каждый человек описывается предикатами:
- `gender(Person, male/female)`
- `birth_year(Person, Year)`
- `alive(Person)` или `death_year(Person, Year)`
- `parent(Parent, Child)`
- `spouse(A, B)`

## 30 правил

| № | Предикат | Значение |
|---|----------|----------|
| 1 | `parent/2` | родитель |
| 2 | `father/2` | отец |
| 3 | `mother/2` | мать |
| 4 | `child/2` | ребёнок |
| 5 | `son/2` | сын |
| 6 | `daughter/2` | дочь |
| 7 | `spouse/2` | супруг(а) |
| 8 | `husband/2` | муж |
| 9 | `wife/2` | жена |
| 10 | `sibling/2` | родной брат/сестра |
| 11 | `brother/2` | брат |
| 12 | `sister/2` | сестра |
| 13 | `grandfather/2` | дедушка |
| 14 | `grandmother/2` | бабушка |
| 15 | `grandchild/2` | внук/внучка |
| 16 | `uncle/2` | дядя |
| 17 | `aunt/2` | тётя |
| 18 | `nephew/2` | племянник |
| 19 | `niece/2` | племянница |
| 20 | `cousin/2` | двоюродный |
| 21 | `ancestor/2` | предок |
| 22 | `descendant/2` | потомок |
| 23 | `alive/1` | жив |
| 24 | `dead/1` | умер |
| 25 | `male/1` | мужской пол |
| 26 | `female/1` | женский пол |
| 27 | `married/1` | женат/замужем |
| 28 | `single/1` | одинок (жив и не женат) |
| 29 | `parent_in_law/2` | свекровь/тёща/свёкор/тесть |
| 30 | `sibling_in_law/2` | шурин/золовка/деверь/свояченица |

## Динамические операции

```prolog
% Рождение нового члена семьи
birth(g4_kirill, g3_michael, g3_oksana, 2020, male).

% Бракосочетание
marriage(g4_nikita, g4_kate).

% Развод
divorce(g4_nikita, g4_kate).

% Смерть
death(g1_fedor, 2005).
```

## Результаты запросов

Ниже представлены результаты всех запросов (запуск в терминале SWI-Prolog).

---

### Запрос 1: Кто мама Анны?
```prolog
?- mother(M, g4_anna).
```
```
Мама Анны: g3_oksana
```

---

### Запрос 2: Кто отец Павла?
```prolog
?- father(F, g4_pavel).
```
```
Отец Павла: g3_michael
```

---

### Запрос 3: Какие дети у Сергея?
```prolog
?- children_of(g2_sergey, Kids).
```
```
Дети Сергея: [g3_michael, g3_andrey]
```

---

### Запрос 4: Есть ли братья у Дмитрия?
```prolog
?- setof(B, brother(B, g3_dmitry), L).
```
```
Братья: [g3_viktor]
```

---

### Запрос 5: Есть ли сёстры у Виктора?
```prolog
?- setof(S, sister(S, g3_viktor), L).
```
```
Сёстры: [g3_olga]
```

---

### Запрос 6: Кто является дедушкой Никиты?
```prolog
?- setof(GF, grandfather(GF, g4_nikita), L).
```
```
Дедушки: [g2_boris]
```

---

### Запрос 7: Кто является бабушкой Софии?
```prolog
?- setof(GB, grandmother(GB, g4_sofia), L).
```
```
Бабушки: [g2_lydia, g2_tamara]
```

---

### Запрос 8: Кто сейчас жив?
```prolog
?- alive_list(Members).
```
```
Живых: 28
Список: [g1_vasilisa, g1_ekaterina, g2_sergey, g2_nina, g2_alexey,
         g2_tamara, g2_boris, g2_lydia, g3_michael, g3_oksana,
         g3_andrey, g3_irina, g3_dmitry, g3_olga, g3_viktor, g3_elena,
         g4_anna, g4_pavel, g4_maria, g4_nikita, g4_sofia, g4_artem,
         g4_kate, g4_leonid, g4_diana, g4_maxim, g4_victoria, g4_danil]
```

---

### Запрос 9: Кто женат?
```prolog
?- married(P).
```
```
g1_fedor, g1_ivan, g2_sergey, g2_alexey, g2_boris,
g3_michael, g3_andrey, g3_dmitry, g3_viktor
```

---

### Запрос 10: Кто одинок (холост и жив)?
```prolog
?- single(P).
```
```
g4_anna (female), g4_pavel (male), g4_maria (female),
g4_nikita (male), g4_sofia (female), g4_artem (male),
g4_kate (female), g4_leonid (male), g4_diana (female),
g4_maxim (male), g4_victoria (female), g4_danil (male)
```

---

### Запрос 11: Все предки Анны
```prolog
?- setof(A, ancestor(A, g4_anna), L).
```
```
Предки: [g1_fedor, g1_vasilisa, g2_alexey, g2_nina, g2_sergey,
         g2_tamara, g3_michael, g3_oksana]
```

---

### Запрос 12: Двоюродные братья/сёстры Артёма
```prolog
?- setof(C, cousin(C, g4_artem), L).
```
```
Двоюродные: [g4_anna, g4_danil, g4_diana, g4_kate, g4_leonid,
             g4_maria, g4_maxim, g4_nikita, g4_pavel]
```

---

### Запрос 13: Тёти и дяди Киры
```prolog
?- setof(A, aunt(A, g4_kate), L1), setof(U, uncle(U, g4_kate), L2).
```
```
Тёти: [g3_elena, g3_oksana]
Дяди: [g3_michael]
```

---

### Запрос 14: Общее число членов семьи
```prolog
?- family_count(N).
```
```
Всего: 30
```

---

### Запрос 15: Дети Оксаны
```prolog
?- setof(C, mother(g3_oksana, C), L).
```
```
Оксана - мать: [g4_anna, g4_maxim, g4_pavel]
```

---

### Запрос 16: Свекрови/тёщи Дмитрия
```prolog
?- setof(PL, parent_in_law(g3_dmitry, PL), L).
```
```
Свекровь/тёща: [g2_boris, g2_lydia]
```

---

### Запрос 17: Шурья/золовка Виктора
```prolog
?- setof(S, sibling_in_law(g3_viktor, S), L).
```
```
Шурин/золовка: [g3_irina, g3_oksana]
```

---

## Результаты динамических операций

```prolog
?- birth(g4_kirill, g3_michael, g3_oksana, 2020, male).
true.
?- parent(g3_michael, g4_kirill).
true.
?- mother(g3_oksana, g4_kirill).
true.

?- death(g1_fedor, 2005).
true.
?- alive(g1_fedor).
false.

?- marriage(g4_nikita, g4_kate).
true.
?- married(g4_nikita).
true.

?- divorce(g4_nikita, g4_kate).
true.
?- married(g4_nikita).
false.
```

---

## Файлы проекта

- `family.pl` — база фактов + 30 правил + динамические операции
- `queries.pl` — 17 запросов для проверки
- `dynamic_demo.pl` — демонстрация birth/marriage/divorce/death
