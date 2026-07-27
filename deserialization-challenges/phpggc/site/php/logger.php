<?php

use Monolog\Handler\StreamHandler;
use Monolog\Logger;

function app_logger(): Logger
{
    static $logger = null;

    if ($logger instanceof Logger) {
        return $logger;
    }

    $logger = new Logger('aste_quadri');
    $logger->pushHandler(new StreamHandler(__DIR__ . '/../private/app.log', Logger::INFO));

    return $logger;
}
