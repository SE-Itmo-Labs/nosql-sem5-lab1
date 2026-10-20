# AGENTS.md | Requirements for AI Agents

If you would ever use AI agents in this project, these agents must obey rules that are written in this document.

## Termins

- Owner - 

- AI bot - 

## General Information

Before working with the any part of the project, etirely read the docs/ folder. This folder contains valuable information. AI bot must follow the project rules. If everything is clear and understandable, supplement the documentation in the doc folder at the end of each work session (update existing documentation, create new documentation). If you see any inconsistencies, report them and suggest corrections and improvements.

## Workflow

### General information

AI Bot should not start, restart, or stop the dev server without a direct user request. For frontend testing, use an existing server on 3000. If it doesn't respond within 5 seconds, report it and continue without attempting to start. Avoid running long-running commands like manage.py runserver in the foreground.

Use browser-use / Playwright / in-app browser only upon explicit user request: "open in browser," "check visually," "take a screenshot," "test frontend." For common, minor HTML/CSS/JS edits, do not automatically launch browser-use/Playwright; limit yourself to code checking and report that visual checking was not performed.

### Backend

Backend server is running at the host port 16767.

### Frontend

Frontend server is running at the port 3000

### Reading

When reading files with Russian text in PowerShell, always use Get-Content -Encoding UTF8; if you see Ð.../unreadable letters, first double-check with -Encoding UTF8 or rg; do not consider the file corrupted based on the output of Get-Content without explicit encoding.

### Editing

Use a DRY approach when developing. Before developing a new feature or inventing a new style, always make sure nothing similar already exists in the project. Do your very best to make code DRY.

When designing new models, normalize them as much as possible. Avoid storing parameters for dozens of different entities in a single class. Make classes as specific as possible. This is necessary to prevent classes from growing in parameters as the project grows and to evenly distribute the database load when there are a large number of active users.

#### Java

Always use short types of imported classes. Example: `Logging log = ...` instead of `java.utils.Logging log = ...`

#### Comments

Add comments to your code to help human developers navigate your codebase more quickly.

### Suggesting

### Testing

Don't use the term "MVP" when working on new parts and features of the project. We don't aim to test hypotheses or create minimum viable versions—we work from the start with the paradigm of "doing it right, making it last, and ensuring architectural stability." Our components are scalable, addressing security concerns and reusability in other areas of the system. During design, we try not to cut functionality, but rather think several steps ahead. If necessary, I will personally define the boundaries of the next feasible version.

### Project scaling

When making product decisions regarding code, architecture, and other matters, keep in mind that we're aiming for 5000 paid users in production. At any given time, 100 people will be using the server. We currently have Rasberry PI 5 with four cores and 6 GB of RAM — let us know if this capacity isn't sufficient for our next innovation.

### Deployment

Before doing anything on any of the production servers, please ask me for confirmation.
