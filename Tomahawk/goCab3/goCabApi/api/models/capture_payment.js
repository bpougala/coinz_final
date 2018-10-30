const express = require('express');
const app = express();
const mysql = require('mysql');
const request = require('request')
const https = require('https')
const querystring = require('querystring')

app.get("/payment/capture/:psp/:amount/:utc", (req, res) => {
    const pspRef = req.params.psp;
    const amount = req.params.amount;
    const utc = req.params.utc;

    const apiUrl = 'pal-test.adyen.com';
    const pathUrl = '/pal/servlet/Payment/v30/authorise';
    const user = 'ws@Company.GoCabAccount132';
    const pass = 'u<*JM3&>HB+y6f[f]sAH+>6hf';
    const auth = 'Basic ' + new Buffer(user + ':' + pass).toString('base64');

    const data = {
        "merchantAccount": "GoCabAPP",

        "modificationAmount": {
            "value": amount,
            "currency": "EUR"
        },

        "originalReference": pspRef,

    };

    const requestHeaders = {
        'Content-Type': 'application/json',
        'Authorization': auth
    };

    console.log(`Headers: ${JSON.stringify(requestHeaders)}`);

    const options = {
        host: apiUrl,
        path: pathUrl,
        method: 'POST',
        headers: requestHeaders
    };



    process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0"; // allow unsafe connections

    var newRequest = https.request(options, (resp) => {
        console.log(`STATUS: ${res.statusCode}`);

        console.log(`HEADERS: ${JSON.stringify(resp.headers)}`);
        resp.setEncoding('utf8');
        resp.on('data', (chunk) => {
            console.log(`BODY: ${chunk}`);
            //res.json(chunk);

            // if the payment was successful, send a confirmation email to the user
            if(chunk.includes("received")) {
                confirmPaymentToUser(pspRef, utc);
            };
        });

        resp.on('end', () => {
            console.log('No more data in response.');
        });
    });

    newRequest.on('error', (e) => {
        console.error(`problem with request: ${e.message}`);
    });

    const dataString = JSON.stringify(data);
    console.log(dataString);

    newRequest.write(dataString);
    newRequest.end();


    // res.end("End of the line, people.");


function confirmPaymentToUser(ref, utc) {
    const options = {
        reference: ref,
        utc: utc
    };

    var request = https.request(options, (resp))
}

});