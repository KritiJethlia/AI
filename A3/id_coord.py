import psycopg2
from time import time

def makeDict():
    '''makes a dictionary with node id as key and tuple of latitude and longitude as value'''
    
    conn = psycopg2.connect(database="final", user = "kriti", password = "root", host = "127.0.0.1", port = "5432")
    t1=time()
    cur = conn.cursor()
    cur.execute("SELECT * FROM planet_osm_nodes;")
    rows = cur.fetchall()
    node_coord={}
    for row in rows :
        node_coord[row[0]]=(row[1],row[2])
    t2=time()
    print(f"took {t2-t1} secs")
    conn.commit()
    conn.close()
    return node_coord


# makeDict()


