% ============================================================
% Тестовые запросы к базе семейного дерева
% ============================================================

run_queries :-
    nl, write('========================================'), nl,
    write('  30 MEMBER FAMILY - TEST QUERIES'), nl,
    write('========================================'), nl,

    % --- Q1: Мама Анны
    nl, write('--- Q1: Кто мама Анны? ---'), nl,
    forall(mother(M, g4_anna),
           (write('  Мама Анны: '), writeln(M))),

    % --- Q2: Папа Павла
    nl, write('--- Q2: Кто отец Павла? ---'), nl,
    forall(father(F, g4_pavel),
           (write('  Отец Павла: '), writeln(F))),

    % --- Q3: Дети Сергея
    nl, write('--- Q3: Дети Сергея ---'), nl,
    children_of(g2_sergey, KidsS),
    write('  Дети Сергея: '), writeln(KidsS),

    % --- Q4: Братья Дмитрия
    nl, write('--- Q4: Братья Дмитрия ---'), nl,
    setof(B, brother(B, g3_dmitry), Brothers),
    write('  Братья: '), writeln(Brothers),

    % --- Q5: Сёстры Виктора
    nl, write('--- Q5: Сёстры Виктора ---'), nl,
    setof(S, sister(S, g3_viktor), Sisters),
    write('  Сёстры: '), writeln(Sisters),

    % --- Q6: Дедушки Никиты
    nl, write('--- Q6: Дедушки Никиты ---'), nl,
    setof(GF, grandfather(GF, g4_nikita), GFs),
    write('  Дедушки: '), writeln(GFs),

    % --- Q7: Бабушки Софии
    nl, write('--- Q7: Бабушки Софии ---'), nl,
    setof(GB, grandmother(GB, g4_sofia), GBs),
    write('  Бабушки: '), writeln(GBs),

    % --- Q8: Все живые
    nl, write('--- Q8: Все живые члены семьи ---'), nl,
    alive_list(Alive),
    write('  Живых: '), length(Alive, N), writeln(N),
    write('  Список: '), writeln(Alive),

    % --- Q9: Женатые
    nl, write('--- Q9: Женатые члены семьи ---'), nl,
    forall((married(P), gender(P, male)),
           (write('  Муж женат: '), writeln(P))),

    % --- Q10: Одинокие
    nl, write('--- Q10: Одинокие (холостые и живые) ---'), nl,
    forall((single(P), gender(P, G)),
           (write('  Одинок: '), write(P), write(' ('), write(G), write(')'), nl)),

    % --- Q11: Все предки Анны
    nl, write('--- Q11: Все предки Анны ---'), nl,
    setof(A, ancestor(A, g4_anna), Anc),
    write('  Предки: '), writeln(Anc),

    % --- Q12: Двоюродные Артёма
    nl, write('--- Q12: Двоюродные Артёма ---'), nl,
    setof(C, cousin(C, g4_artem), Cous),
    write('  Двоюродные: '), writeln(Cous),

    % --- Q13: Тёти/дяди Киры
    nl, write('--- Q13: Тёти и дяди Киры ---'), nl,
    setof(A, aunt(A, g4_kate), Aunts),
    write('  Тёти: '), writeln(Aunts),
    setof(U, uncle(U, g4_kate), Uncles),
    write('  Дяди: '), writeln(Uncles),

    % --- Q14: Общее число членов
    nl, write('--- Q14: Общее число членов семьи ---'), nl,
    family_count(Cnt),
    write('  Всего: '), writeln(Cnt),

    % --- Q15: Оксана - мать?
    nl, write('--- Q15: Дети Оксаны ---'), nl,
    setof(Child, mother(g3_oksana, Child), OkKids),
    write('  Оксана - мать: '), writeln(OkKids),

    % --- Q16: Свекрови/тёщи Дмитрия
    nl, write('--- Q16: Свекрови/тёщи Дмитрия (parent_in_law) ---'), nl,
    setof(PL, parent_in_law(g3_dmitry, PL), PLs),
    write('  Свекровь/тёща: '), writeln(PLs),

    % --- Q17: Шурья/золовки Виктора
    nl, write('--- Q17: Шурин/золовка Виктора (sibling_in_law) ---'), nl,
    setof(SIL, sibling_in_law(g3_viktor, SIL), SILs),
    write('  Шурин/золовка: '), writeln(SILs),

    nl, write('========================================'), nl,
    write('  ALL QUERIES COMPLETED'), nl,
    write('========================================'), nl.
