import os, json, urllib.request
key=os.environ.get('GEMINI_API_KEY','')
req=urllib.request.Request(
  'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent',
  data=json.dumps({'contents':[{'parts':[{'text':'Reply with exactly OK'}]}]}).encode(),
  headers={'Content-Type':'application/json','x-goog-api-key':key},
  method='POST')
try:
  with urllib.request.urlopen(req, timeout=30) as r:
    print(r.read()[:400].decode())
except Exception as e:
  print('ERR', e)
