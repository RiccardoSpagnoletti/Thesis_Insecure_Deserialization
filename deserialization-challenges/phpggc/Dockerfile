FROM php:8.1-apache

RUN apt-get update \
    && apt-get install -y --no-install-recommends unzip libsqlite3-dev exim4-daemon-light \
    && rm -rf /var/lib/apt/lists/*

RUN docker-php-ext-install pdo_sqlite sockets \
    && a2enmod rewrite \
    && sed -i 's/AllowOverride None/AllowOverride All/g' /etc/apache2/apache2.conf

COPY --from=composer:latest /usr/bin/composer /usr/bin/composer

WORKDIR /var/www/html
ENV COMPOSER_ALLOW_SUPERUSER=1

COPY site/composer.json site/composer.lock ./
RUN composer install \
    --no-dev \
    --prefer-dist \
    --no-interaction \
    --no-progress \
    --no-plugins \
    --no-scripts

COPY site ./

RUN chown -R www-data:www-data /var/www/html/data /var/www/html/private

EXPOSE 80
CMD ["apache2-foreground"]

