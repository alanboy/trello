@echo off

IF "%1"=="pomodoro" (
    copy /y code.py_pomodoro d:\code.py
) ELSE IF "%1"=="rest" (
    copy /y code.py_rest d:\code.py
) ELSE IF "%1"=="standby" (
    copy /y code.py_standby d:\code.py
)


