#!/bin/bash

# Função para rodar o setup
run_setup() {
  # Espera 30 segundos. O SQL Server é pesado e demora para iniciar.
  # Este tempo deve ser maior que o 'WAITFOR DELAY' do seu script.
  echo "Aguardando 30s para o SQL Server iniciar..."
  sleep 30s
  
  echo "Tentando executar setup.sql..."
  # Roda o script de setup.sql
  # O -i aponta para o script que já está montado no container
  /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "SuaSenhaForte123" -i /docker-entrypoint-initdb.d/setup.sql -N -C
  
  if [ $? -eq 0 ]; then
    echo "setup.sql executado com sucesso."
  else
    echo "Falha ao executar setup.sql."
  fi
}

# Roda a função de setup em background (&)
run_setup &

# Inicia o SQL Server no foreground (este é o comando principal)
# O 'exec' garante que ele seja o processo principal (PID 1) do container
echo "Iniciando SQL Server..."
exec /opt/mssql/bin/sqlservr