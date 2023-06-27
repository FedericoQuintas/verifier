#/bin/sh
curl -i -X POST \
   -H "Content-Type:multipart/form-data" \
   -F "file=@\"./$1\";type=text/plain;filename=\"$1\"" \
 'http://localhost:8080/upload'