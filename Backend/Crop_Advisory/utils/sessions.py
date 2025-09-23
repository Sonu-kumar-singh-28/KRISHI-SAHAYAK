from uuid import uuid4
active_session = {}

def create_session():
    session_id = str(uuid4())
    active_session[session_id] = {"is_verified": False, "user_data": None, "phone_number": None}
    return session_id

def delete_session(session_id):
    if session_id in active_session:
        del active_session[session_id]