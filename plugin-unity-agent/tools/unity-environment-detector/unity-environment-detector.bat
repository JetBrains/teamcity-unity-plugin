@echo off

IF %UNITY_ROOT_PARAMETER%=="" (
    CALL :find_in_directory "%ProgramFiles%"
    CALL :find_in_directory "%ProgramFiles(X86)%"
) ELSE (
    CALL :find_in_directory %UNITY_ROOT_PARAMETER%
)

EXIT /B %ERRORLEVEL%

:find_in_directory
FOR /F "tokens=* USEBACKQ" %%F IN (`where /r "%~1" Unity.exe 2^> nul`) DO (
    if ERRORLEVEL 0 (
        SET UNITY_PATH=%%F
        FOR /F "tokens=* USEBACKQ" %%F IN (`"%UNITY_PATH%" -version 2^> nul`) DO (
            if ERRORLEVEL 0 (
                SET UNITY_VERSION=%%F
                CALL :echo_results "%UNITY_PATH%" "%UNITY_VERSION%"
            )
        )
    )
)
EXIT /B 0

:echo_results
  echo path=%~1;version=%~2
EXIT /B 0