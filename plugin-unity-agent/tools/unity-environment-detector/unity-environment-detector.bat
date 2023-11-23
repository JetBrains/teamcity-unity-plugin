@echo off

IF "%UNITY_ROOT_PARAMETER%"=="" (
    CALL :log "Searching in program files"
    CALL :find_in_directory "%ProgramFiles%"
    CALL :find_in_directory "%ProgramFiles(X86)%"
) ELSE (
    CALL :log "Searching in the specified root %UNITY_ROOT_PARAMETER%"
    CALL :find_in_directory %UNITY_ROOT_PARAMETER%
)

EXIT /B %ERRORLEVEL%

:find_in_directory
@REM Immediate expansion is the default (all variables are substituted during parse time) in batch scripts.
@REM Since we are evaluating UNITY_PATH in a loop below, we need to do it at runtime with the help of enclosing the variable in '!'
SETLOCAL EnableDelayedExpansion
FOR /F "tokens=* USEBACKQ delims=" %%F IN (`where /r "%~1" Unity.exe 2^> nul`) DO (
    if ERRORLEVEL 0 (
        SET UNITY_PATH=%%F
        CALL :log "Found Unity at path: !UNITY_PATH!"
        FOR /F "tokens=* USEBACKQ delims=" %%F IN (`"!UNITY_PATH!" -version 2^> nul`) DO (
            if ERRORLEVEL 0 (
                SET UNITY_VERSION=%%F
                CALL :echo_results "!UNITY_PATH!" "!UNITY_VERSION!"
            )
        )
    )
)
ENDLOCAL
EXIT /B 0

:log
  echo "log:%~1"
EXIT /B 0

:echo_results
  echo path=%~1;version=%~2
EXIT /B 0