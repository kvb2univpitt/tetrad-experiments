mkdir venv
python3 -m venv venv

source venv/bin/activate

pip install --upgrade pip
pip install wheel
pip install -U scikit-learn
pip install numpy
pip install -U matplotlib

pip install ipykernel

pip install -U autopep8
