% ============================================================
% ЛАБОРАТОРНАЯ РАБОТА: Семейное дерево на Prolog
% 30 членов семьи, 4 поколения
% 30 правил/фактов + операции birth, marriage, divorce, death
% ============================================================

:- dynamic parent/2.
:- dynamic spouse/2.
:- dynamic alive/1.
:- dynamic gender/2.
:- dynamic birth_year/2.
:- dynamic death_year/2.

:- discontiguous parent/2.
:- discontiguous spouse/2.
:- discontiguous alive/1.
:- discontiguous gender/2.
:- discontiguous birth_year/2.
:- discontiguous death_year/2.

% ============================================================
% Генерация 1 — прадедушки и прабабушки (4 человека)
% ============================================================
gender(g1_fedor, male).
gender(g1_vasilisa, female).
gender(g1_ivan, male).
gender(g1_ekaterina, female).

birth_year(g1_fedor, 1930).
birth_year(g1_vasilisa, 1932).
birth_year(g1_ivan, 1928).
birth_year(g1_ekaterina, 1935).

death_year(g1_fedor, 2005).
death_year(g1_ivan, 2010).

alive(g1_vasilisa).
alive(g1_ekaterina).

% ============================================================
% Генерация 2 — дедушки и бабушки (6 человек)
% ============================================================
gender(g2_sergey, male).
gender(g2_nina, female).
gender(g2_alexey, male).
gender(g2_tamara, female).
gender(g2_boris, male).
gender(g2_lydia, female).

birth_year(g2_sergey, 1955).
birth_year(g2_nina, 1957).
birth_year(g2_alexey, 1952).
birth_year(g2_tamara, 1954).
birth_year(g2_boris, 1958).
birth_year(g2_lydia, 1960).

alive(g2_sergey).
alive(g2_nina).
alive(g2_alexey).
alive(g2_tamara).
alive(g2_boris).
alive(g2_lydia).

% ============================================================
% Генерация 3 — родители (8 человек)
% ============================================================
gender(g3_michael, male).
gender(g3_oksana, female).
gender(g3_andrey, male).
gender(g3_irina, female).
gender(g3_dmitry, male).
gender(g3_olga, female).
gender(g3_viktor, male).
gender(g3_elena, female).

birth_year(g3_michael, 1980).
birth_year(g3_oksana, 1982).
birth_year(g3_andrey, 1978).
birth_year(g3_irina, 1981).
birth_year(g3_dmitry, 1985).
birth_year(g3_olga, 1987).
birth_year(g3_viktor, 1983).
birth_year(g3_elena, 1986).

alive(g3_michael).
alive(g3_oksana).
alive(g3_andrey).
alive(g3_irina).
alive(g3_dmitry).
alive(g3_olga).
alive(g3_viktor).
alive(g3_elena).

% ============================================================
% Генерация 4 — дети (12 человек)
% ============================================================
gender(g4_anna, female).
gender(g4_pavel, male).
gender(g4_maria, female).
gender(g4_nikita, male).
gender(g4_sofia, female).
gender(g4_artem, male).
gender(g4_kate, female).
gender(g4_leonid, male).
gender(g4_diana, female).
gender(g4_maxim, male).
gender(g4_victoria, female).
gender(g4_danil, male).

birth_year(g4_anna, 2005).
birth_year(g4_pavel, 2007).
birth_year(g4_maria, 2008).
birth_year(g4_nikita, 2010).
birth_year(g4_sofia, 2012).
birth_year(g4_artem, 2015).
birth_year(g4_kate, 2018).
birth_year(g4_leonid, 2006).
birth_year(g4_diana, 2009).
birth_year(g4_maxim, 2011).
birth_year(g4_victoria, 2014).
birth_year(g4_danil, 2017).

alive(g4_anna).
alive(g4_pavel).
alive(g4_maria).
alive(g4_nikita).
alive(g4_sofia).
alive(g4_artem).
alive(g4_kate).
alive(g4_leonid).
alive(g4_diana).
alive(g4_maxim).
alive(g4_victoria).
alive(g4_danil).

% ============================================================
% Facts: parent/2  (parent(Parent, Child))
% ============================================================

% Дети generation 1 -> generation 2
parent(g1_fedor, g2_sergey).
parent(g1_vasilisa, g2_sergey).
parent(g1_fedor, g2_alexey).
parent(g1_vasilisa, g2_alexey).
parent(g1_ivan, g2_boris).
parent(g1_ekaterina, g2_boris).

% Дети generation 2 -> generation 3
parent(g2_sergey, g3_michael).
parent(g2_nina, g3_michael).
parent(g2_alexey, g3_oksana).
parent(g2_tamara, g3_oksana).
parent(g2_sergey, g3_andrey).
parent(g2_nina, g3_andrey).
parent(g2_alexey, g3_irina).
parent(g2_tamara, g3_irina).
parent(g2_boris, g3_dmitry).
parent(g2_lydia, g3_dmitry).
parent(g2_boris, g3_olga).
parent(g2_lydia, g3_olga).
parent(g2_boris, g3_viktor).
parent(g2_lydia, g3_viktor).
parent(g2_alexey, g3_elena).
parent(g2_tamara, g3_elena).

% Дети generation 3 -> generation 4
parent(g3_michael, g4_anna).
parent(g3_oksana, g4_anna).
parent(g3_michael, g4_pavel).
parent(g3_oksana, g4_pavel).
parent(g3_andrey, g4_maria).
parent(g3_irina, g4_maria).
parent(g3_dmitry, g4_nikita).
parent(g3_olga, g4_nikita).
parent(g3_viktor, g4_sofia).
parent(g3_elena, g4_sofia).
parent(g3_viktor, g4_artem).
parent(g3_elena, g4_artem).
parent(g3_andrey, g4_kate).
parent(g3_irina, g4_kate).
parent(g3_dmitry, g4_leonid).
parent(g3_olga, g4_leonid).
parent(g3_andrey, g4_diana).
parent(g3_irina, g4_diana).
parent(g3_michael, g4_maxim).
parent(g3_oksana, g4_maxim).
parent(g3_viktor, g4_victoria).
parent(g3_elena, g4_victoria).
parent(g3_dmitry, g4_danil).
parent(g3_olga, g4_danil).

% ============================================================
% Facts: spouse/2  (bidirectional)
% ============================================================
spouse(g1_fedor, g1_vasilisa).
spouse(g1_vasilisa, g1_fedor).
spouse(g1_ivan, g1_ekaterina).
spouse(g1_ekaterina, g1_ivan).
spouse(g2_sergey, g2_nina).
spouse(g2_nina, g2_sergey).
spouse(g2_alexey, g2_tamara).
spouse(g2_tamara, g2_alexey).
spouse(g2_boris, g2_lydia).
spouse(g2_lydia, g2_boris).
spouse(g3_michael, g3_oksana).
spouse(g3_oksana, g3_michael).
spouse(g3_andrey, g3_irina).
spouse(g3_irina, g3_andrey).
spouse(g3_dmitry, g3_olga).
spouse(g3_olga, g3_dmitry).
spouse(g3_viktor, g3_elena).
spouse(g3_elena, g3_viktor).

% ============================================================
% 30 RULES
% ============================================================

% 1. parent(Parent, Child) — факт

% 2. father(Father, Child)
father(X, Y) :- parent(X, Y), gender(X, male).

% 3. mother(Mother, Child)
mother(X, Y) :- parent(X, Y), gender(X, female).

% 4. child(Child, Parent)
child(X, Y) :- parent(Y, X).

% 5. son(Son, Parent)
son(X, Y) :- parent(Y, X), gender(X, male).

% 6. daughter(Daughter, Parent)
daughter(X, Y) :- parent(Y, X), gender(X, female).

% 7. spouse(A, B) — факт

% 8. husband(Husband, Wife)
husband(X, Y) :- spouse(X, Y), gender(X, male).

% 9. wife(Wife, Husband)
wife(X, Y) :- spouse(X, Y), gender(X, female).

% 10. sibling(A, B)
sibling(X, Y) :- parent(Z, X), parent(Z, Y), X \= Y.

% 11. brother(Brother, Person)
brother(X, Y) :- sibling(X, Y), gender(X, male).

% 12. sister(Sister, Person)
sister(X, Y) :- sibling(X, Y), gender(X, female).

% 13. grandfather(GF, Grandchild)
grandfather(X, Y) :- parent(X, Z), parent(Z, Y), gender(X, male).

% 14. grandmother(GB, Grandchild)
grandmother(X, Y) :- parent(X, Z), parent(Z, Y), gender(X, female).

% 15. grandchild(Grandchild, GP)
grandchild(X, Y) :- parent(Y, Z), parent(Z, X).

% 16. uncle(Uncle, Nephew/Niece)
uncle(X, Y) :- brother(X, Z), parent(Z, Y).

% 17. aunt(Aunt, Nephew/Niece)
aunt(X, Y) :- sister(X, Z), parent(Z, Y).

% 18. nephew(Nephew, UncleOrAunt)
nephew(X, Y) :- sibling(Y, Z), parent(Z, X), gender(X, male).

% 19. niece(Niece, UncleOrAunt)
niece(X, Y) :- sibling(Y, Z), parent(Z, X), gender(X, female).

% 20. cousin(A, B)
cousin(X, Y) :- parent(A, X), parent(B, Y), sibling(A, B), X \= Y.

% 21. ancestor(Ancestor, Descendant)
ancestor(X, Y) :- parent(X, Y).
ancestor(X, Y) :- parent(X, Z), ancestor(Z, Y).

% 22. descendant(Descendant, Ancestor)
descendant(X, Y) :- parent(Y, X).
descendant(X, Y) :- parent(Y, Z), descendant(X, Z).

% 23. alive(Person) — факт

% 24. dead(Person)
dead(X) :- death_year(X, _).

% 25. male(Person)
male(X) :- gender(X, male).

% 26. female(Person)
female(X) :- gender(X, female).

% 27. married(Person)
married(X) :- spouse(X, _).

% 28. single(Person)
single(X) :- alive(X), \+ married(X).

% 29. parent_in_law(X, Y)
parent_in_law(X, Y) :- spouse(X, Z), parent(Y, Z).

% 30. sibling_in_law(X, Y)
sibling_in_law(X, Y) :- spouse(X, Z), sibling(Y, Z).

% ============================================================
% Dynamic operations
% ============================================================

% birth(Child, Father, Mother, BirthYear, Gender)
birth(Child, Father, Mother, Year, G) :-
    assertz(parent(Father, Child)),
    assertz(parent(Mother, Child)),
    assertz(gender(Child, G)),
    assertz(birth_year(Child, Year)),
    assertz(alive(Child)).

% marriage(Person1, Person2)
marriage(P1, P2) :-
    assertz(spouse(P1, P2)),
    assertz(spouse(P2, P1)).

% divorce(Person1, Person2)
divorce(P1, P2) :-
    retract(spouse(P1, P2)),
    retract(spouse(P2, P1)).

% death(Person, Year)
death(Person, Year) :-
    (retract(alive(Person)) ; true),
    assertz(death_year(Person, Year)).

% ============================================================
% Utility
% ============================================================
family_member(X) :- gender(X, _).
family_count(N) :- findall(X, family_member(X), L), length(L, N).
children_of(Parent, Children) :- findall(C, parent(Parent, C), Children).
alive_list(Members) :- findall(X, alive(X), Members).
