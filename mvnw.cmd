@REM ----------------------------------------------------------------------------
@REM CleanLoop Maven Wrapper Script
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "MVN_EXEC=mvn"
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Users\aksha\apache-maven-3.9.9\bin\mvn.cmd" (
        set "MVN_EXEC=C:\Users\aksha\apache-maven-3.9.9\bin\mvn.cmd"
    )
)

"%MVN_EXEC%" %*
exit /b %ERRORLEVEL%
