from mysql import connector
from mysqlx import errorcode
def select_from_db(query):
    try:
        cnx = connector.connect(user='root',
                                password='1234',
                                host="localhost",
                                port=3306,
                                database='word_duel')
    except connector.Error as err:
        if err.errno == connector.errorcode.ER_ACCESS_DENIED_ERROR:
            print("Something is wrong with your user name or password")
        elif err.errno == errorcode.ER_BAD_DB_ERROR:
            print("Database does not exist")
        else:
            print(err)
    else:
        cnx.close()
        cnx.reconnect()
        cursor = cnx.cursor()
        cursor.execute(query)
        return cursor.fetchall()

def insert_to_db(query):
    try:
        cnx = connector.connect(user='root',
                                  password='1234',
                                  host="localhost",
                                    port=3306,
                                  database='word_duel')
    except connector.Error as err:
        if err.errno == connector.errorcode.ER_ACCESS_DENIED_ERROR:
            print("Something is wrong with your user name or password")
        elif err.errno == errorcode.ER_BAD_DB_ERROR:
            print("Database does not exist")
        else:
            print(err)
    else:
        cnx.close()

    cnx.reconnect()
    cursor = cnx.cursor()
    cursor.execute(query)
    cnx.commit()
