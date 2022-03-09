"""
Search for a name in the next week minister roster within the latest Warta GKI Cawang 

@author yohanes.gultom@gmail.com
"""

import requests
import re
import numpy as np
from pathlib import Path
from datetime import datetime
from bs4 import BeautifulSoup
from tabula import read_pdf

def download_file_from_google_drive(id, destination):
    def get_confirm_token(response):
        for key, value in response.cookies.items():
            if key.startswith('download_warning'):
                return value
        return None

    def save_response_content(response, destination):
        CHUNK_SIZE = 32768
        with open(destination, "wb") as f:
            for chunk in response.iter_content(CHUNK_SIZE):
                if chunk: # filter out keep-alive new chunks
                    f.write(chunk)

    URL = "https://drive.google.com/u/0/uc?&export=download"
    session = requests.Session()
    response = session.get(URL, params = { 'id' : id }, stream = True)
    token = get_confirm_token(response)
    if token:
        params = { 'id' : id, 'confirm' : token }
        response = session.get(URL, params = params, stream = True)

    save_response_content(response, destination) 

def get_latest_wp_post_url(base_url, category):
    '''
    Get the latest wordpress post for certain category ID
    '''
    api_url = "{}/wp-json/wp/v2/posts?_fields=id,date,link,categories&orderby=date&order=desc&categories={}&per_page=1".format(base_url, category)
    res = requests.get(api_url)
    data = res.json()
    post_date = datetime.strptime(data[0]['date'], '%Y-%m-%dT%H:%M:%S')
    post_url = data[0]['link']
    return post_date, post_url

def get_doc_id_from_post(post_url):
    '''
    Find Google Docs ID within a link in Wordpress Post page pointed by post_url
    '''
    doc_id, doc_url, label = (None, None, None)
    page = requests.get(post_url)
    soup = BeautifulSoup(page.text, 'html.parser')
    links = soup.select('.post_content a')
    pattern = re.compile("/d/([^\s]+)/")
    for link in links:
        if 'warta jemaat' in link.string.lower():
            match = pattern.search(link['href'])
            if match:
                label = link.string
                doc_url = link['href']
                doc_id = match[1]
                break
    return doc_id, doc_url, label
            
def download_latest_warta():
    '''
    Download latest warta in gki-cawang.org
    '''
    # 7 = warta jemaat
    base_url = 'http://gki-cawang.org'
    post_date, post_url = get_latest_wp_post_url(base_url, 7)
    # print('post_date: {}'.format(post_date))
    print('post_url: {}'.format(post_url))
    doc_id, doc_url, label = get_doc_id_from_post(post_url)
    # print('doc_id: {}'.format(doc_id))
    print('doc_url: {}'.format(doc_url))
    # print('label: {}'.format(label))
    dest = 'WARTA_{}.PDF'.format(post_date.strftime('%Y%m%d'))
    # download if not yet done
    if not Path(dest).is_file():
        print('Downloading...')
        download_file_from_google_drive(doc_id, dest)
    # print('{} succesfully downloaded'.format(dest))
    return dest, post_date

def search_name_in_warta(path, search_name):
    '''
    Search search_name in certain page of PDF in given path
    '''
    results = []
    service_date, task_name, service_no, original_text = (None, None, None, None)
    date_pattern = re.compile("\((\w+,\s*\d{1,2}\s*\w+\s*\d{4})\)")
    dataframes = read_pdf(path, pages="all", silent=True)
    for df in dataframes:
        if not df.empty:
            res = [df[col].astype(str).str.contains(search_name, na=False, flags=re.IGNORECASE) for col in df]
            mask = np.column_stack(res)
            df_found = df.loc[mask.any(axis=1)]
            if not df_found.empty:
                arr = df_found.dropna(axis=1).to_numpy(dtype=np.unicode_)
                match_text = ' '.join(arr.flatten())
                print(match_text)
                results.append(match_text)
    return results
            

if __name__ == "__main__":
    import sys
    query = sys.argv[1]
    dest, post_date = download_latest_warta()
    # dest, post_date = ('WARTA_20200201.PDF', datetime.now())
    results = search_name_in_warta(dest, query)
    # print('Latest post date: {}'.format(post_date))
    # if original_text:        
    #     print(service_date)
    #     print('Tugas {}'.format(task_name))
    #     print('Kebaktian ke-{}'.format(service_no))
    #     print(original_text)
    # else:
    #     print('Tidak ditemukan')