import skygear

from .my_file import my_string

@skygear.handler("test-string")
def myString(req):
    return my_string
