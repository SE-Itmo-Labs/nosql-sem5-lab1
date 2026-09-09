:- ['family.pl'].

run_demo :-
    nl, write('========================================'), nl,
    write('  DYNAMIC OPERATIONS DEMO'), nl,
    write('========================================'), nl,

    % --- birth ---
    nl, write('--- DEMO: birth ---'), nl,
    birth(g4_kirill, g3_michael, g3_oksana, 2020, male),
    write('  parent(michael, kirill): '),
    (parent(g3_michael, g4_kirill) -> writeln('true') ; writeln('false')),
    write('  mother(oksana, kirill):   '),
    (mother(g3_oksana, g4_kirill) -> writeln('true') ; writeln('false')),
    write('  alive(kirill):            '),
    (alive(g4_kirill) -> writeln('true') ; writeln('false')),

    % --- death ---
    nl, write('--- DEMO: death ---'), nl,
    death(g1_fedor, 2005),
    write('  dead(fedor):   '),
    (dead(g1_fedor) -> writeln('true') ; writeln('false')),
    write('  alive(fedor):  '),
    (alive(g1_fedor) -> writeln('true') ; writeln('false')),

    % --- marriage ---
    nl, write('--- DEMO: marriage ---'), nl,
    marriage(g4_nikita, g4_kate),
    write('  spouse(nikita, kate): '),
    (spouse(g4_nikita, g4_kate) -> writeln('true') ; writeln('false')),
    write('  married(nikita):      '),
    (married(g4_nikita) -> writeln('true') ; writeln('false')),
    write('  wife(kate, nikita):   '),
    (wife(g4_kate, g4_nikita) -> writeln('true') ; writeln('false')),

    % --- divorce ---
    nl, write('--- DEMO: divorce ---'), nl,
    divorce(g4_nikita, g4_kate),
    write('  spouse(nikita, kate):  '),
    (spouse(g4_nikita, g4_kate) -> writeln('true') ; writeln('false')),
    write('  married(nikita):       '),
    (married(g4_nikita) -> writeln('true') ; writeln('false')),

    nl, write('========================================'), nl,
    write('  DYNAMIC DEMO COMPLETED'), nl,
    write('========================================'), nl.
