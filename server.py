
import json
from bottle import route, run, request, response
from warta import download_latest_warta, search_name_in_warta

@route('/')
def index():
    response.set_header('Content-Type', 'application/json')    
    try:
        if not request.query.query:
            raise Exception('missing query')
        query = request.query.query
        dest, post_date = download_latest_warta()
        service_date, task_name, service_no, original_text = search_name_in_warta(dest, query)
        return json.dumps({
            'query': query,
            'service_date': service_date,
            'task_name': task_name,
            'service_no': service_no,
            'original_text': original_text,
        })
    except Exception as err:
        response.status = 500
        return json.dumps({'error': str(err)})

run(host='localhost', server='tornado', port=5000)