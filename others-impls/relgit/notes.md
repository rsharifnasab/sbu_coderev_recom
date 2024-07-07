## DOTNET

install powershell, dotnet-runtime and dotnet-sdk and mssql-tools, dotnet-sdk-2.2

add to path:
```bash
export PATH="$PATH:/home/roozbeh/.dotnet/tools"
```

run powershell with:
```bash
pwsh
```

## RelationalGit

```bash

dotnet tool install --global RelationalGit
# XOR
dotnet tool install --global SofiaWL --version 2.0.4 # for sofia

```

set this env var:

```bash
export DOTNET_SYSTEM_GLOBALIZATION_INVARIANT=1
```

dotnet-rgit --conf-path "C:\Users\Ehsan Mirsaeedi\Documents\relationalgit.json"  --cmd get-github-pullrequest-reviewer-comments



## DB

do not use docker-compose to set up sql-server

use this docker command:

```bash
sudo docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=<YourStrong@Passw0rd>" -p 1433:1433 --name sql1 --hostname sql1 -d mcr.microsoft.com/mssql/server:2017-latest

```

connect to db:
```bash
sqlcmd -P "<YourStrong@Passw0rd>" -U SA -H 127.0.0.1:1433 -C
```
