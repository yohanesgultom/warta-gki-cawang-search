
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
        post_date_str = post_date.strftime('%Y-%m-%dT%H:%M:%S') if post_date else None
        results = search_name_in_warta(dest, query)
        return json.dumps({
            'query': query,
            'post_date': post_date_str,
            'results': results,
        })
    except Exception as err:
        response.status = 500
        return json.dumps({'error': str(err)})

if __name__ == "__main__":
    import sys
    host = sys.argv[1] if len(sys.argv) > 1 else '0.0.0.0'
    port = sys.argv[2] if len(sys.argv) > 2 else 5000
    run(host=host, server='tornado', port=port)