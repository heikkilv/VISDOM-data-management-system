FROM python:3.7.13-alpine

WORKDIR /

RUN mkdir -p converter
COPY requirements.txt /converter
RUN python3 -m pip install -r converter/requirements.txt
COPY converter.py /converter

CMD [ "python3", "-u", "-m", "converter.converter" ]
