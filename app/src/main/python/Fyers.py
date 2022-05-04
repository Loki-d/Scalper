from fyers_api import fyersModel
from fyers_api import accessToken

class fyers:
    def __init__(self):
        self.session = None
        self.access_token = ""
        self.fyers = None
        self.client_id = "<client id>"
        self.secret_key = "<secret id>"
        self.login_success = False

    def fetch_authcode_url(self):
        self.session = accessToken.SessionModel(
            client_id = self.client_id,
            secret_key = self.secret_key,
            redirect_uri = "https://127.0.0.1/",
            response_type = "code",
            grant_type = "authorization_code"
        )
        response = self.session.generate_authcode()
        return response

    def fetch_access_token(self, auth_code):
        self.session.set_token(auth_code)
        response = self.session.generate_token()
        self.access_token = response["access_token"]
        return self.access_token

    def init_fyersmodel(self, access_token, log_path):
        self.access_token = access_token
        self.fyers = fyersModel.FyersModel(client_id=self.client_id, token=self.access_token, log_path=log_path)
        response = self.fyers.get_profile()
        status_code = response.get('code')
        if status_code == 200:
            self.login_success = True
        return status_code

if __name__ == "__main__":
    pass