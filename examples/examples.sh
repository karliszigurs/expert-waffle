#!/bin/bash

curl -0 -v http://localhost:8080/accounts \
  -H 'Content-Type: application/json; charset=utf-8' \
  -d @client_with_accounts.json | jq .

curl -0 -v http://localhost:8080/accounts \
  -H 'Content-Type: application/json; charset=utf-8' \
  -d @client_without_accounts.json | jq .

  curl -0 -v http://localhost:8080/accounts \
    -H 'Content-Type: application/json; charset=utf-8' \
    -d @invalid_client_id.json | jq .

curl -0 -v http://localhost:8080/transfer \
    -H 'Content-Type: application/json; charset=utf-8' \
    -d @transfer_with_conversion.json | jq .

curl -0 -v http://localhost:8080/transfer \
    -H 'Content-Type: application/json; charset=utf-8' \
    -d @transfer_without_conversion.json | jq .

curl -0 -v http://localhost:8080/transfer \
    -H 'Content-Type: application/json; charset=utf-8' \
    -d @transfer_from_empty_account.json | jq .

curl -0 -v http://localhost:8080/transfer \
    -H 'Content-Type: application/json; charset=utf-8' \
    -d @transfer_from_unknown_currency.json | jq .

curl -0 -v http://localhost:8080/transfer \
    -H 'Content-Type: application/json; charset=utf-8' \
    -d @transfer_to_unknown_currency.json | jq .

curl -0 -v http://localhost:8080/history \
    -H 'Content-Type: application/json; charset=utf-8' \
    -d @transfer_to_unknown_currency.json | jq .

curl -0 -v http://localhost:8080/history \
    -H 'Content-Type: application/json; charset=utf-8' \
    -d @history.json | jq .

curl -0 -v http://localhost:8080/history \
    -H 'Content-Type: application/json; charset=utf-8' \
    -d @history_with_limits.json | jq .

curl -0 -v http://localhost:8080/history \
    -H 'Content-Type: application/json; charset=utf-8' \
    -d @history_of_unknown_account.json | jq .
